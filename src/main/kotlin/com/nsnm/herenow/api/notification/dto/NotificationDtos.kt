package com.nsnm.herenow.api.notification.dto

import com.nsnm.herenow.lib.model.entity.NotificationType
import java.time.LocalDateTime

data class DeviceTokenRequest(
    val expoPushToken: String
)

data class NotificationResponse(
    val notificationId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val targetId: String?,
    val isRead: Boolean,
    val createdAt: LocalDateTime?
)
