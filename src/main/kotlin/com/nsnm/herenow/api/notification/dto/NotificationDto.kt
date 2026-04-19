package com.nsnm.herenow.api.notification.dto

import java.time.OffsetDateTime
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val type: String,
    val title: String,
    val body: String,
    val targetId: UUID?,
    val isRead: Boolean,
    val createdAt: OffsetDateTime
)
