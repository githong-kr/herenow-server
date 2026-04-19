package com.nsnm.herenow.api.ai.dto

data class AiChatRequest(val message: String)
data class AiChatResponse(val responseMessage: String)

data class AiAnalyzeRequest(val imageUrl: String)
data class AiAnalyzeResponse(
    val itemName: String?,
    val suggestedCategory: String?,
    val suggestedExpiry: String?,
    val description: String?
)
