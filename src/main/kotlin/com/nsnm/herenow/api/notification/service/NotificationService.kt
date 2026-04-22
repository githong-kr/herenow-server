package com.nsnm.herenow.api.notification.service

import com.nsnm.herenow.api.notification.dto.*
import com.nsnm.herenow.domain.notification.entity.NotificationEntity
import com.nsnm.herenow.domain.notification.repository.NotificationRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {

    fun getNotifications(userId: UUID): List<NotificationResponse> {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).map { it.toResponse() }
    }

    @Transactional
    fun markAsRead(userId: UUID, notificationId: UUID): NotificationResponse {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.") }
        if (notification.userId != userId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.")
        notification.isRead = true
        notificationRepository.save(notification)
        return notification.toResponse()
    }
}
