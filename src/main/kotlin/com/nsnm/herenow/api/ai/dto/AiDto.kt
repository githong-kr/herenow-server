package com.nsnm.herenow.api.ai.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 프론트엔드가 백엔드로 보내는 Vision 분석 요청
 */
data class AnalyzeItemRequest(
    @Schema(description = "분석할 물건의 이미지 URL (Supabase Public URL 등)", example = "https://pqax.../storage/v1/object/public/items/123.jpg")
    val imageUrl: String
)

/**
 * 프론트엔드에 응답할 최종 분석 결과 포맷
 */
data class AnalyzeItemResponse(
    @Schema(description = "추출된 물건 이름", example = "다우니 섬유유연제 1L")
    val itemName: String?,
    @Schema(description = "매칭되거나 새로 생성된 카테고리 ID")
    val categoryId: String?,
    @Schema(description = "매칭되거나 새로 생성된 장소 ID")
    val locationId: String?,
    @Schema(description = "예상 수량", example = "1")
    val quantity: Int?,
    @Schema(description = "경고 알림 수량(최소 주문 수량)", example = "0")
    val minQuantity: Int?,
    @Schema(description = "유통/소비기한 (YYYY-MM-DD 형식)", example = "2026-12-31")
    val expiryDate: String?,
    @Schema(description = "분석된 추천 태그 목록", example = "[\"다우니\", \"세탁용품\"]")
    val tags: List<String>?,
    @Schema(description = "메모 또는 추가 정보", example = "용량이 1L인 실내용 섬유유연제입니다.")
    val memo: String?
)

/**
 * 프론트엔드가 백엔드로 보내는 대화형 채팅 요청
 */
data class AiChatRequest(
    @Schema(description = "사용자의 질문", example = "섬유유연제 지금 몇 개 남아있어?")
    val message: String
)

/**
 * 프론트엔드에 응답할 AI 채팅 결과
 */
data class AiChatResponse(
    @Schema(description = "AI의 대답 텍스트")
    val answer: String
)

/**
 * 프론트엔드가 백엔드로 보내는 음성 명령 (App Actions) 요청
 */
data class AiVoiceCommandRequest(
    @Schema(description = "음성 인식된 사용자의 발화 텍스트 (명령어)", example = "1번 물건 하나 빼줘")
    val message: String
)

/**
 * 음성 명령 실행 완료 후 프론트엔드에 응답할 결과
 */
data class AiVoiceCommandResponse(
    @Schema(description = "명령 수행 결과 메시지", example = "우유(1번) 수량을 1개 뺐어요.")
    val responseMessage: String
)

/**
 * AI가 반환할 순수 JSON Schema 모델 (내부 처리용)
 */
data class GeminiAiOutputFormat(
    val itemName: String = "",
    val categoryName: String = "",
    val categoryGroup: String = "",
    val locationName: String = "",
    val locationGroup: String = "",
    val quantity: Int = 1,
    val minQuantity: Int = 0,
    val expiryDate: String = "",
    val tags: List<String> = emptyList(),
    val memo: String = ""
)

// ==========================================
// Google Gemini API Request / Response DTOs
// ==========================================

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    val mimeType: String,
    val data: String // Base64 encoded byte array
)

data class GeminiGenerationConfig(
    val responseMimeType: String = "application/json"
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContentResponse? = null,
    val finishReason: String? = null
)

data class GeminiContentResponse(
    val parts: List<GeminiPartResponse>? = null,
    val role: String? = null
)

data class GeminiPartResponse(
    val text: String? = null
)

data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
