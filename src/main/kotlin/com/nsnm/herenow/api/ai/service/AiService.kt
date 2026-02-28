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

        val categoriesPromptList = existingCategories.joinToString(", ") { "[그룹: ${it.categoryGroup ?: "미지정"}, 분류명: ${it.categoryName}]" }
        val locationsPromptList = existingLocations.joinToString(", ") { "[그룹: ${it.locationGroup ?: "미지정"}, 장소명: ${it.locationName}]" }

        // 3. 커스텀 프롬프트 구성 (Vision용 시스템 명령어)
        val promptText = """
            당신은 스마트 인벤토리 앱의 비전 및 정리 전문가입니다.
            사용자가 스토리지/보관함에 등록할 물건의 이미지를 보냈습니다.
            이미지를 주의 깊게 분석하여 다음 사항들을 추론하세요.
            
            요구사항:
            1. 'itemName': 상품의 구체적인 이름(브랜드가 보이면 포함, 예: '다우니 실내건조 1L')
            2. 'categoryName': 이 물건의 소분류 분류명. 사용자의 분류 목록 $categoriesPromptList 을 참고하여 매칭시키되, 없다면 짤막하고 직관적인 '단일 명사' 수준의 새 소분류명을 제안하세요. (예: '선풍기')
            3. 'categoryGroup': 'categoryName'이 속할 대분류 그룹명. 새 대분류를 제안할 경우 '단일 명사' 위주로 직관적으로 정하되, **절대 'categoryName'과 단어가 중복되지 않게** 하세요. (예: 소분류가 '선풍기'라면 대분류는 '가전' 또는 '전자기기')
            4. 'locationName': 이 물건을 보관할 상세 장소명. 사용자의 장소 목록 $locationsPromptList 을 참고하여 매칭시키되, 없다면 '단일 명사' 수준의 짧은 장소명을 제안하세요. (예: '서랍')
            5. 'locationGroup': 'locationName'이 속할 주거 구역/방 이름(대분류). 새 대분류를 제안할 경우 **절대 'locationName'과 단어가 중복되지 않게** '단일 명사'로 정하세요. (예: 상세 장소가 '서랍'이라면 대분류는 '거실' 또는 '안방')
            6. 'quantity': 육안으로 확인되는 대략적인 물품의 개수 (정수형). 모호하면 1을 주세요.
            7. 'minQuantity': 재고가 떨어졌을 때 알림을 받을 경고 수량(최소 주문 수량, 정수형). 보통 0 또는 1로 설정하세요.
            8. 'expiryDate': 이미지에서 음식물 등 명확한 유통기한이나 소비기한이 보인다면 'YYYY-MM-DD' 형식으로 표기하세요. 판단하기 어렵다면 빈 문자열 처리.
            9. 'tags': 물건의 검색 태그 1~3개. (예: ["세탁용품", "다우니"])
            10. 'memo': 물건 상태나 특징 등 기억할 만한 메모를 1~2문장으로 짧게 요약하세요.
            
            중요: 응답은 다른 설명 없이 오직 RFC 8259 호환 순수 JSON 포맷으로만 제출하세요.
            결과 형식: {"itemName": "", "categoryName": "", "categoryGroup": "", "locationName": "", "locationGroup": "", "quantity": 1, "minQuantity": 0, "expiryDate": "", "tags": [], "memo": ""}
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

        println("aiOutput : $aiOutput")

        // 7. 카테고리명 / 장소명 Auto Create 로직 (공백 제거 적용)
        val aiCategoryName = aiOutput.categoryName.trim()
        val aiLocationName = aiOutput.locationName.trim()
        val aiCategoryGroup = aiOutput.categoryGroup.trim()
        val aiLocationGroup = aiOutput.locationGroup.trim()

        var matchedCategoryId = existingCategories.find { it.categoryName == aiCategoryName }?.categoryId
        var matchedLocationId = existingLocations.find { it.locationName == aiLocationName }?.locationId

        // 매칭 실패 시 즉시 자동 생성 (Option B: 기존에 없으면 생성)
        if (matchedCategoryId == null && aiCategoryName.isNotBlank()) {
            val newCat = CategoryEntity(
                groupId = groupId,
                categoryName = aiCategoryName,
                categoryGroup = aiCategoryGroup.takeIf { it.isNotBlank() },
                displayOrder = (existingCategories.maxOfOrNull { it.displayOrder } ?: 0) + 1
            )
            val savedCat = categoryRepository.save(newCat)
            matchedCategoryId = savedCat.categoryId
        }

        if (matchedLocationId == null && aiLocationName.isNotBlank()) {
            val newLoc = LocationEntity(
                groupId = groupId,
                locationName = aiLocationName,
                locationGroup = aiLocationGroup.takeIf { it.isNotBlank() },
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
                val category = categories[item.categoryId]
                val categoryName = category?.categoryName ?: "분류 없음"
                val categoryGroup = category?.categoryGroup?.let { " ($it)" } ?: ""
                
                val location = locations[item.locationId]
                val locationName = location?.locationName ?: "위치 지정 안됨"
                val locationGroup = location?.locationGroup?.let { " ($it)" } ?: ""
                
                "- [${item.itemName}] (분류: $categoryName$categoryGroup, 위치: $locationName$locationGroup) 수량: ${item.quantity}개, 기록/메모: ${item.memo ?: "없음"}"
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
            - 물건을 찾으면 어디에 보관되어 있는지(가능하다면 대분류 구역 위치도 함께), 수량이 얼마나 남았는지 친근하고 짧은 어투로 바로 말해주세요.
            - 특정 장소(예: 서랍)에 어떤 물건이 있는지 묻는 질문이라면, 해당 장소에 속한 물건들의 목록을 안내해 주세요.
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

    /**
     * App Actions (음성 명령) 처리 로직
     */
    @Transactional
    fun executeVoiceCommand(groupId: String, request: AiVoiceCommandRequest): AiVoiceCommandResponse {
        if (geminiApiKey.isBlank()) {
            throw BizException("제미나이 API 키가 서버에 설정되어 있지 않습니다.")
        }

        // 1. 현재 사용자 보관함 전체 스냅샷 조회 (단축 번호 포함)
        val items = itemRepository.findByGroupId(groupId)
        
        val inventoryContext = if (items.isEmpty()) {
            "보관함에 물건이 없습니다."
        } else {
            items.joinToString("\n") { item ->
                "ID: ${item.itemId}, 단축번호(shortcutNumber): ${item.shortcutNumber ?: "없음"}, 이름: ${item.itemName}, 현재수량: ${item.quantity}개"
            }
        }

        // 2. Gemini에게 DB 수정을 위한 파라미터(JSON)를 강제하는 프롬프트 작성
        val promptText = """
            당신은 스마트 인벤토리 앱의 데이터베이스 수정 AI입니다.
            사용자가 음성으로 보관함 물품의 수량을 변경하거나 삭제하라는 명령을 내렸습니다.
            <명령> ${request.message} </명령>
            
            <현재_보관함_상태>
            $inventoryContext
            </현재_보관함_상태>
            
            명령을 분석하여 어떤 물건(ID)의 수량을 어떻게 바꿀지 정확히 1개만 택해서 JSON으로 출력하세요.
            (단축번호나 이름의 유사도로 추론하세요)
            
            출력 포맷 (반드시 JSON만 출력):
            {
              "targetItemId": "", // 변경할 물건의 ID 문자열 (찾지 못한 경우 null)
              "resultingQuantity": 0, // 연산이 끝난 후 최종 반영될 수량 (예: 현재 3개인데 '하나 썼어'라면 2). 수량이 0 이하라면 0으로 세팅.
              "isDelete": false, // 사용자가 아예 삭제하라고 명시적으로 지시한 경우만 true
              "message": "" // 사용자에게 들려줄 친절한 한국어 응답 1문장 (예: '우유(1번) 수량을 1개 뺐어요.')
            }
        """.trimIndent()

        val geminiReq = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = promptText))
                )
            ),
            generationConfig = GeminiGenerationConfig() // responseMimeType = application/json
        )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val httpEntity = HttpEntity(geminiReq, headers)

        // 3. API 전송
        val response = try {
            restTemplate.exchange(
                GEMINI_API_URL + geminiApiKey,
                HttpMethod.POST,
                httpEntity,
                GeminiResponse::class.java
            ).body ?: throw BizException("AI 응답이 비어있습니다.")
        } catch (e: Exception) {
            throw BizException("음성 명령 처리 API 호출 중 오류가 발생했습니다: ${e.message}")
        }

        if (response.error != null) {
            throw BizException(response.error.message ?: "알 수 없는 AI 오류")
        }

        val outputText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw BizException("AI 결과 텍스트를 추출할 수 없습니다.")

        data class VoiceCommandDecision(
            val targetItemId: String?,
            val resultingQuantity: Int,
            val isDelete: Boolean,
            val message: String
        )

        val decision: VoiceCommandDecision = try {
            objectMapper.readValue(outputText)
        } catch (e: Exception) {
            throw BizException("AI가 수행할 명령을 이해하지 못했습니다: $outputText")
        }

        if (decision.targetItemId == null) {
             return AiVoiceCommandResponse(responseMessage = decision.message.ifBlank { "해당하는 물건을 보관함에서 찾을 수 없어요." })
        }

        // 5. 실제 DB 트랜잭션 수행
        val targetItem = itemRepository.findById(decision.targetItemId).orElse(null)
            ?: return AiVoiceCommandResponse(responseMessage = "지정된 물건이 이미 삭제되었거나 존재하지 않습니다.")

        if (decision.isDelete) {
            itemRepository.delete(targetItem)
        } else {
            targetItem.quantity = decision.resultingQuantity.coerceAtLeast(0)
            itemRepository.save(targetItem)
        }

        return AiVoiceCommandResponse(responseMessage = decision.message)
    }
}
