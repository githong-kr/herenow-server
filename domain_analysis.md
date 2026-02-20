# HereNow 도메인 분석 보고서

기존 프론트엔드 레포지토리(`herenow-frontend/database.types.ts`)의 Supabase 스키마를 정밀 분석한 결과, 본 프로젝트의 핵심 도메인은 당초 예상되었던 "미션/기록(Mission/Record)" 관리가 아니라, **"사용자 간의 물품(Item) 공유 및 재고 관리"** 서비스임이 확인되었습니다.

## 1. 핵심 비즈니스 도메인 및 테이블 구조

전체 스키마는 크게 **[물품 관리]**, **[사용자 및 그룹]**, **[부가 기능]** 세 가지 영역으로 나눌 수 있습니다.

### 1) 물품 관리 도메인 (Items & Inventory)
가장 핵심이 되는 데이터 축으로, 그룹 내에서 물품의 위치, 카테고리, 태그 등을 관리합니다.
*   **`items`**: 핵심 엔티티. 각 물품의 이름, 가격, 수량(`quantity`), 최소 수량(`min_quantity`), 구매처, 유통기한(`expiry_date`) 등을 보관합니다.
*   **`categories`**: 물품의 종류를 트리 구조(부모 카테고리 ID 포함)로 분류합니다.
*   **`locations`**: 물품이 보관된 장소(예: 냉장고, 창고 등)를 관리합니다. 트리 구조 분할이 가능합니다.
*   **`tags` & `item_tags`**: 물품에 다중 태그를 부여하여 검색과 필터링을 용이하게 합니다.
*   **`item_photos`**: 물품의 상태나 영수증 등 다중 사진(URL)을 보관합니다.
*   **`item_history`**: 수량 변경이나 항목 수정 등 물품 엔티티의 변경 이력을 추적합니다 (Auditing).
*   **`favorites`**: 특정 사용자가 자주 참조하는 물품을 즐겨찾기(스크랩) 합니다.

### 2) 사용자 및 그룹 도메인 (Users & Groups)
이 서비스는 단일 사용자 기반이 아닌, 가족/회사 등 다중 사용자가 공간을 공유하는 **B2B / B2C 그룹 웨어** 형태를 띕니다.
*   **`profiles`**: 사용자 기본 정보(이름, 프로필 사진, 구독 여부 등)를 담고 있습니다.
*   **`user_groups`**: 물품을 공유하는 공간 단위(그룹)입니다. 
*   **`user_group_members`**: 그룹에 속한 사용자들의 권한(`owner`, `member`)을 매핑합니다. (이 그룹 ID가 `items`, `categories`, `locations` 등에 모두 FK로 걸려있어 데이터 격리를 수행합니다)
*   **`group_invitations`**: 그룹 초대를 위한 토큰 기반의 초대장 테이블입니다.

### 3) 부가 및 인프라 도메인 (Add-ons & Infra)
*   **`notifications` & `push_subscriptions`**: 재고 부족이나 유통기한 임박에 대한 푸시 알림 전송 내역을 관리합니다. (알림 생성 시 외부 HTTP Webhook으로 푸시 전송)
*   **`payments`**: 서비스 구독 결제 내역(Toss Payments 등 연동 추정)을 저장합니다.
*   **`short_urls`**: 그룹 초대 링크 등을 배포할 때 사용하는 단축 URL 테이블입니다.
*   **`transactions`**: API 호출/에러 내역 등을 기록하는 시스템 로그 테이블입니다. (우리가 앞서 백엔드에서 만든 `api_call_log`와 유사한 역할. 5일 이상 된 로그 자동 삭제 로직 존재)

## 2. 기존 플랫폼 빌트인(DB Trigger/Function) 정책 분석

원본 Supabase 데이터베이스에는 여러 가지 자동화된 비즈니스 룰이 트리거(Trigger)와 데이터베이스 내장 함수(Function) 형태로 구현되어 있었습니다. 이 부분은 모두 Spring Boot 서비스 계층(Service Layer)에서 이식하여 구현해야 합니다.

1.  **사용자 가입 자동화 (`handle_sign_up` 함수)**
    *   신규 회원이 가입하면(Sign Up) 자동으로 **익명(혹은 가입자 이름)의 기본 공간(`User Group`)**을 생성합니다.
    *   방금 가입한 사용자를 해당 공간의 **방장(Owner) 멤버**로 매핑합니다.
    *   그 공간을 사용자의 **대표 그룹(`representative_group_id`)**으로 자동 지정합니다.
2.  **데이터 변경 추적 (Item Auditing - `item_change_function`, `item_before_delete_function`)**
    *   `items`, `item_photos`, `item_tags`, `tags` 테이블에 C/U/D (생성, 수정, 삭제)가 일어날 때마다 **`item_history` 테이블에 기존 값(`previous_values`)과 변경 값(`new_values`)을 JSON 형태로 자동 기록**합니다.
    *   단일 트랜잭션에서 여러 개가 수정/삭제될 때 하나의 `batch_id`로 묶어서 기록합니다.
3.  **외부 스토리지 및 알림 연동 (Webhook Triggers)**
    *   **사진 삭제**: `item_photos` 테이블의 행이 지워지면, `https://herenow.nsnm.xyz/api/cron/cleanup-item-storage` API를 호출하여 실제 스토리지 파일도 삭제되도록 연동.
    *   **푸시 알림**: `notifications` 테이블에 행이 추가되면, `https://herenow.nsnm.xyz/api/push-trigger` API를 찔러서 실제 모바일/웹 푸시를 발송토록 연동.

---

## 3. 주요 기능 액션 목록 (API 구현 대상)

위 구조 및 비즈니스 룰들을 지원하기 위해 스프링 부트(Spring Boot) 백엔드로 이전(Migration)해야 할 **주요 API 기능 목록**은 다음과 같습니다.

### [User / Group API]
1.  **그룹 생성 및 멤버 관리**: 새 그룹(공간) 생성, 사용자 초대 링크(토큰) 발급, 초대 수락 및 멤버 조인.
2.  **프로필 및 권한 조회**: 내 프로필 수정, 현재 활성화된(선택된) 그룹 조회 및 소속 권한(Owner/Member) 체크.

### [Item API]
1.  **아이템 CRUD**: 새로운 물품 등록, 수량 조정(재고 관리), 수정, 삭제.
2.  **메타데이터 관리**: 물품 등록 전제조건인 카테고리(Categories), 보관장소(Locations), 태그(Tags)의 CRUD.
3.  **목록 및 검색 (List View)**: `item_list_view` 뷰(View)를 대체하거나 직접 쿼리하여, 특정 그룹 내의 아이템 목록을 카테고리/장소/태그/즐겨찾기 여부 등으로 다중 필터링하는 복합 조회 액션.
4.  **히스토리 조회**: 특정 아이템의 수량 증감 기록 내역 로드 (`item_history`).
5.  **사진 첨부**: 아이템 사진 업로드 (S3 등 스토리지 연동) 및 메타데이터(`item_photos`) 갱신.

### [Notification API]
1.  **웹푸시 구독**: 브라우저/모바일별 푸시 토큰(`push_subscriptions`) 등록.
2.  **알림 발송/조회**: 유통기한 임박, 품절(최소 수량 미달) 시 푸시 알림 트리거 및 내 알림 목록(`notifications`) 조회.

---

## 4. 백엔드(서버) 스펙 이식 방향성 (제안)

이 분석을 바탕으로, **Phase 4 비즈니스 구현**은 다음 순서로 차근차근 뼈대를 올리는 것을 제안합니다.

1.  **프라이머리 도메인 (Group & Metadatas)**
    *   모든 데이터의 뿌리가 되는 **`Group`** 관련 API부터 뚫습니다. (유저는 헤더의 JWT로 식별)
    *   아이템의 부속품인 **`Category`, `Location`, `Tag`** CRUD API를 작성합니다.
2.  **코어 도메인 (Items & History)**
    *   본론인 **`Item`** 엔티티와 API를 설계합니다. 등록 시 사진(`item_photos`), 히스토리(`item_history`) 삽입을 서비스 트랜잭션(`@Transactional`)으로 묶어 안전하게 처리합니다.
3.  **조회(View) 최적화 도메인**
    *   기존 Supabase의 `item_list_view` 뷰를 대체할 수 있는 복합 조회 API(QueryDSL 적용 고려)를 작성합니다.

> 검토 후, 어떤 도메인(예: `Item`부터? 아니면 기초가 되는 `Category/Location`부터?)을 먼저 서버 쪽 엔티티와 Controller/Service 코드로 이식할지 지시해 주시면 즉각 구현을 시작하겠습니다!
