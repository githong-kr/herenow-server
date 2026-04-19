package com.nsnm.herenow.api.ai.service

import com.nsnm.herenow.api.ai.dto.*
import com.nsnm.herenow.domain.space.repository.SpaceMemberRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class AiService(
    private val spaceMemberRepository: SpaceMemberRepository
) {

    fun chat(userId: UUID, spaceId: UUID, req: AiChatRequest): AiChatResponse {
        requireMembership(userId, spaceId)
        // TODO: Gemini API 연동
        return AiChatResponse(responseMessage = "AI 챗봇 기능은 준비 중입니다. 요청: ${req.message}")
    }

    fun analyze(userId: UUID, spaceId: UUID, req: AiAnalyzeRequest): AiAnalyzeResponse {
        requireMembership(userId, spaceId)
        // TODO: Vision API 연동
        return AiAnalyzeResponse(
            itemName = null,
            suggestedCategory = null,
            suggestedExpiry = null,
            description = "사진 분석 기능은 준비 중입니다."
        )
    }

    private fun requireMembership(userId: UUID, spaceId: UUID) {
        if (!spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "스페이스 멤버가 아닙니다.")
    }
}
