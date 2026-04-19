package com.nsnm.herenow.api.ai.controller

import com.nsnm.herenow.api.ai.dto.*
import com.nsnm.herenow.api.ai.service.AiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/spaces/{spaceId}/ai")
class AiController(
    private val aiService: AiService
) {

    @PostMapping("/chat")
    fun chat(
        @PathVariable spaceId: UUID,
        @RequestBody req: AiChatRequest,
        principal: Principal
    ): ResponseEntity<AiChatResponse> {
        return ResponseEntity.ok(aiService.chat(UUID.fromString(principal.name), spaceId, req))
    }

    @PostMapping("/analyze")
    fun analyze(
        @PathVariable spaceId: UUID,
        @RequestBody req: AiAnalyzeRequest,
        principal: Principal
    ): ResponseEntity<AiAnalyzeResponse> {
        return ResponseEntity.ok(aiService.analyze(UUID.fromString(principal.name), spaceId, req))
    }
}
