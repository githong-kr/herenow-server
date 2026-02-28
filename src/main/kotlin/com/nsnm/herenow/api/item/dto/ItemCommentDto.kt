package com.nsnm.herenow.api.item.dto

data class ItemCommentCreateRequest(
    val content: String
)

data class ItemCommentResponse(
    val commentId: String,
    val itemId: String,
    val groupId: String,
    val writerId: String,
    val writerName: String,
    val writerAvatarUrl: String?,
    val content: String,
    val createdAt: String
)
