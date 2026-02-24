package com.nsnm.herenow.api.ai.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nsnm.herenow.api.ai.dto.*
import com.nsnm.herenow.domain.item.model.entity.CategoryEntity
import com.nsnm.herenow.domain.item.model.entity.LocationEntity
import com.nsnm.herenow.domain.item.repository.CategoryRepository
import com.nsnm.herenow.domain.item.repository.ItemRepository
import com.nsnm.herenow.domain.item.repository.LocationRepository
import com.nsnm.herenow.fwk.core.error.BizException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.util.Base64

@Service
class AiService(
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val itemRepository: ItemRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${app.gemini.api-key:}")
    private val geminiApiKey: String
) {
    private val restTemplate = RestTemplate()
    private val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="

    @Transactional
    fun analyzeItemImage(groupId: String, request: AnalyzeItemRequest): AnalyzeItemResponse {
        if (geminiApiKey.isBlank()) {
            throw BizException("제미나이 API 키가 서버에 설정되어 있지 않습니다.")
        }

        // 1. Supabase 이미지 다운로드 및 Base64 인코딩
        val imageBytes = try {
            restTemplate.getForObject(request.imageUrl, ByteArray::class.java)
                ?: throw BizException("이미지 버퍼를 읽을 수 없습니다.")
        } catch (e: Exception) {
            throw BizException("이미지 다운로드 중 오류가 발생했습니다: ${e.message}")
        }
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        // URL에서 확장자를 추론하거나 기본 jpeg 사용
        val mimeType = if (request.imageUrl.lowercase().endsWith(".png")) "image/png" else "image/jpeg"

        // 2. 그룹의 현재 카테고리 / 장소 목록 조회 (컨텍스트 주입 용도)
        val existingCategories = categoryRepository.findByGroupId(groupId).sortedBy { it.displayOrder }
        val existingLocations = locationRepository.findByGroupId(groupId).sortedBy { it.displayOrder }

        val categoriesPromptList = existingCategories.joinToString(", ") { "[\"${it.categoryName}\"]" }
        val locationsPromptList = existingLocations.joinToString(", ") { "[\"${it.locationName}\"]" }

        // 3. 커스텀 프롬프트 구성 (Vision용 시스템 명령어)
        val promptText = """
            당신은 스마트 인벤토리 앱의 비전 및 정리 전문가입니다.
            사용자가 스토리지/보관함에 등록할 물건의 이미지를 보냈습니다.
            이미지를 주의 깊게 분석하여 다음 사항들을 추론하세요.
            
            요구사항:
            1. 'itemName': 상품의 구체적인 이름(브랜드가 보이면 포함, 예: '다우니 실내건조 1L')
            2. 'categoryName': 이 물건의 분류명. 사용자의 카테고리 목록 $categoriesPromptList 중 하나와 완벽히 일치하면 그 이름을 사용하고, 없다면 짤막하고 상식적인 새로운 카테고리명을 제안하세요.
            3. 'locationName': 이 물건을 보통 어디에 보관하는지. 사용자의 보관 장소 목록 $locationsPromptList 중 하나와 완벽히 일치하면 그 이름을 사용하고, 없다면 짧은 보관 장소명을 제안하세요.
            4. 'quantity': 육안으로 확인되는 대략적인 물품의 개수 (정수형). 모호하면 1을 주세요.
            5. 'minQuantity': 재고가 떨어졌을 때 알림을 받을 경고 수량(최소 주문 수량, 정수형). 보통 0 또는 1로 설정하세요.
            6. 'expiryDate': 이미지에서 유통기한이나 소비기한이 명확하게 보인다면 'YYYY-MM-DD' 형식으로 표기하세요. 음식물과 같이 통상적인 유통기한이나 소비기한이 있다면 알려주세요. 판단하기 어렵다면 빈 문자열.
            7. 'tags': 이 물건과 관련된 검색 태그들을 1~3개 정도 배열로 제공하세요. (예: ["세탁용품", "다우니"])
            8. 'memo': 물건 상태나 특징 등 기억할 만한 메모를 1~2문장으로 요약하세요.
            
            중요: 응답은 다른 설명 없이 오직 RFC 8259 호환 순수 JSON 포맷으로만 제출하세요.
            결과 형식: {"itemName": "", "categoryName": "", "locationName": "", "quantity": 1, "minQuantity": 0, "expiryDate": "", "tags": [], "memo": ""}
        """.trimIndent()

        // 4. Gemini 요청 조립
        val geminiReq = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = promptText),
                        GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Image))
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig() // responseMimeType = "application/json"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val httpEntity = HttpEntity(geminiReq, headers)

        // 5. API 전송 및 파싱
        val response = try {
            restTemplate.exchange(
                GEMINI_API_URL + geminiApiKey,
                HttpMethod.POST,
                httpEntity,
                GeminiResponse::class.java
            ).body ?: throw BizException("AI 응답이 비어있습니다.")
        } catch (e: Exception) {
            throw BizException("AI 분석 API 호출 중 오류가 발생했습니다: ${e.message}")
        }

        if (response.error != null) {
            throw BizException(response.error.message ?: "알 수 없는 AI 오류")
        }

        val outputText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw BizException("AI 결과 텍스트를 추출할 수 없습니다.")

        // 6. JSON 언패킹 매핑
        val aiOutput: GeminiAiOutputFormat = try {
            objectMapper.readValue(outputText)
        } catch (e: Exception) {
            // 모델이 JSON 규격을 간혹 어겼을 경우
            throw BizException("AI가 올바른 JSON 포맷을 반환하지 않았습니다: $outputText")
        }

        // 7. 카테고리명 / 장소명 Auto Create 로직
        var matchedCategoryId = existingCategories.find { it.categoryName == aiOutput.categoryName }?.categoryId
        var matchedLocationId = existingLocations.find { it.locationName == aiOutput.locationName }?.locationId

        // 매칭 실패 시 즉시 자동 생성 (Option B: 기존에 없으면 생성)
        if (matchedCategoryId == null && aiOutput.categoryName.isNotBlank()) {
            val newCat = CategoryEntity(
                groupId = groupId,
                categoryName = aiOutput.categoryName,
                displayOrder = (existingCategories.maxOfOrNull { it.displayOrder } ?: 0) + 1
            )
            val savedCat = categoryRepository.save(newCat)
            matchedCategoryId = savedCat.categoryId
        }

        if (matchedLocationId == null && aiOutput.locationName.isNotBlank()) {
            val newLoc = LocationEntity(
                groupId = groupId,
                locationName = aiOutput.locationName,
                displayOrder = (existingLocations.maxOfOrNull { it.displayOrder } ?: 0) + 1
            )
            val savedLoc = locationRepository.save(newLoc)
            matchedLocationId = savedLoc.locationId
        }

        return AnalyzeItemResponse(
            itemName = aiOutput.itemName,
            categoryId = matchedCategoryId,
            locationId = matchedLocationId,
            quantity = aiOutput.quantity,
            minQuantity = aiOutput.minQuantity,
            expiryDate = aiOutput.expiryDate,
            tags = aiOutput.tags,
            memo = aiOutput.memo
        )
    }

    @Transactional(readOnly = true)
    fun chatWithInventory(groupId: String, request: AiChatRequest): AiChatResponse {
        if (geminiApiKey.isBlank()) {
            throw BizException("제미나이 API 키가 서버에 설정되어 있지 않습니다.")
        }

        // 1. 현재 사용자의 스마트 인벤토리(보관함) 데이터 전체 스냅샷 조회
        val items = itemRepository.findByGroupId(groupId)
        val categories = categoryRepository.findByGroupId(groupId).associateBy { it.categoryId }
        val locations = locationRepository.findByGroupId(groupId).associateBy { it.locationId }

        // 2. AI에게 주입할 CSV/텍스트 기반 컨텍스트 문자열 생성
        val inventoryContext = if (items.isEmpty()) {
            "현재 보관함에 등록된 물건이 하나도 없습니다."
        } else {
            items.joinToString("\n") { item ->
                val categoryName = categories[item.categoryId]?.categoryName ?: "카테고리 없음"
                val locationName = locations[item.locationId]?.locationName ?: "위치 지정 안됨"
                "- [${item.itemName}] (분류: $categoryName, 위치: $locationName)수량: ${item.quantity}개, 기록/메모: ${item.memo ?: "없음"}"
            }
        }

        // 3. 챗봇 페르소나 및 데이터 프롬프팅
        val promptText = """
            당신은 사용자의 집(또는 보관 스페이스) 전체 물건을 꿰뚫고 있는 친절한 '스마트 인벤토리 비서'입니다.
            현재 이 계정(보관함)의 실시간 물품 데이터베이스 내입력 정보는 다음과 같습니다:
            
            <InventoryDatabase>
            $inventoryContext
            </InventoryDatabase>
            
            사용자의 질문: "${request.message}"
            
            지시사항:
            - 오직 위 <InventoryDatabase> 데이터만을 기반으로 질문에 대답하세요.
            - 물건을 찾으면 어디에 보관되어 있는지, 수량이 얼마나 남았는지 친근하고 짧은 어투로 바로 말해주세요.
            - 보관함에 없는 물건을 찾으면 "목록에서 찾을 수 없다"고 분명히 말하세요.
            - 사용자가 메모나 특징을 묻는다면 꼼꼼히 확인해서 답변하세요.
            - 대답은 3~4문장 이내로 핵심만, 매우 자연스러운 한국어 구어체(~해요, ~입니다)로 작성하세요. 마크다운 사용(볼드체 등) 가능합니다.
        """.trimIndent()

        // 4. Gemini 요청 조립
        val geminiReq = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = promptText))
                )
            )
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val httpEntity = HttpEntity(geminiReq, headers)

        // 5. API 전송
        val response = try {
            restTemplate.exchange(
                GEMINI_API_URL + geminiApiKey,
                HttpMethod.POST,
                httpEntity,
                GeminiResponse::class.java
            ).body ?: throw BizException("AI 응답이 비어있습니다.")
        } catch (e: Exception) {
            throw BizException("채팅 API 호출 중 오류가 발생했습니다: ${e.message}")
        }

        if (response.error != null) {
            throw BizException(response.error.message ?: "알 수 없는 AI 오류")
        }

        val answerText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw BizException("AI 결과 텍스트를 추출할 수 없습니다.")

        return AiChatResponse(answer = answerText)
    }
}
