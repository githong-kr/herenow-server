package com.nsnm.herenow.api.ai.controller

import com.nsnm.herenow.api.ai.dto.*
import com.nsnm.herenow.api.ai.service.AiService
import com.nsnm.herenow.fwk.core.base.BaseController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "AI", description = "AI 연동 및 제미나이 기능 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/ai")
class AiController(
    private val aiService: AiService
) : BaseController() {

    @Operation(summary = "물건 이미지 판독 (Vision AI)", description = "Gemini 1.5 Flash Vision 모델을 활용하여 사진속 물품을 분석하고 적절한 카테고리, 메모, 장소를 자동 완성합니다.")
    @PostMapping("/analyze-item")
    fun analyzeItemImage(
        @PathVariable groupId: String,
        @RequestBody request: AnalyzeItemRequest
    ): AnalyzeItemResponse {
        return aiService.analyzeItemImage(groupId, request)
    }

    @Operation(summary = "보관함 스마트 비서 채팅 (Chat AI)", description = "현재 그룹(스페이스)의 모든 물품 정보를 AI에게 컨텍스트로 전달하여 자연어 질의응답을 처리합니다.")
    @PostMapping("/chat")
    fun chatWithInventory(
        @PathVariable groupId: String,
        @RequestBody request: AiChatRequest
    ): AiChatResponse {
        return aiService.chatWithInventory(groupId, request)
    }

    @Operation(summary = "음성 제어 명령 (App Actions)", description = "Google Assistant 등을 통해 들어온 사용자의 자연어 명령을 분석하여, 보관함 데이터(수량 조절 등)를 직접 수정하고 결과를 텍스트로 반환합니다.")
    @PostMapping("/voice-command")
    fun executeVoiceCommand(
        @PathVariable groupId: String,
        @RequestBody request: AiVoiceCommandRequest
    ): AiVoiceCommandResponse {
        return aiService.executeVoiceCommand(groupId, request)
    }
}
