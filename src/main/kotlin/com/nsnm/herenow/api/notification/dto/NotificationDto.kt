package com.nsnm.herenow.api.notification.dto

import java.time.OffsetDateTime
import java.util.UUID
import com.nsnm.herenow.domain.notification.entity.NotificationEntity

data class NotificationResponse(
    val id: UUID,
    val type: String,
    val title: String,
    val body: String,
    val targetId: UUID?,
    val isRead: Boolean,
    val createdAt: OffsetDateTime
)

fun NotificationEntity.toResponse() = NotificationResponse(
    id = id, type = type, title = title, body = body,
    targetId = targetId, isRead = isRead, createdAt = createdAt
)
