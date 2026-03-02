package com.nsnm.herenow.api.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nsnm.herenow.api.notification.dto.DeviceTokenRequest
import com.nsnm.herenow.api.notification.dto.NotificationResponse
import com.nsnm.herenow.domain.notification.repository.NotificationRepository
import com.nsnm.herenow.domain.user.repository.DeviceTokenRepository
import com.nsnm.herenow.fwk.core.base.BaseService
import com.nsnm.herenow.fwk.core.error.BizException
import com.nsnm.herenow.lib.model.entity.DeviceTokenEntity
import com.nsnm.herenow.lib.model.entity.NotificationEntity
import com.nsnm.herenow.lib.model.entity.NotificationType
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID

@Service
class NotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val notificationRepository: NotificationRepository,
    private val webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper
) : BaseService() {

    private val expoPushUrl = "https://exp.host/--/api/v2/push/send"

    @Transactional
    fun registerDeviceToken(profileId: String, request: DeviceTokenRequest) {
        val token = request.expoPushToken
        if (!deviceTokenRepository.existsByExpoPushToken(token)) {
            val entity = DeviceTokenEntity(
                tokenId = UUID.randomUUID().toString(),
                profileId = profileId,
                expoPushToken = token
            )
            deviceTokenRepository.save(entity)
            log.info("New Expo Push Token registered for user: \$profileId")
        }
    }

    @Transactional(readOnly = true)
    fun getMyNotifications(profileId: String, pageable: Pageable): Page<NotificationResponse> {
        val result = notificationRepository.findByProfileIdOrderByFrstRegTmstDesc(profileId, pageable)
        return result.map { entity ->
            NotificationResponse(
                notificationId = entity.notificationId,
                title = entity.title,
                body = entity.body,
                type = entity.type,
                targetId = entity.targetId,
                isRead = entity.isRead,
                createdAt = entity.frstRegTmst
            )
        }
    }

    @Transactional
    fun markAsRead(notificationId: String) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { BizException("알림을 찾을 수 없습니다.") }
        notification.isRead = true
        notificationRepository.save(notification)
    }

    @Transactional
    fun markAllAsRead(profileId: String) {
        notificationRepository.markAllAsReadByProfileId(profileId)
    }

    /**
     * 알림 생성 및 Expo 푸시 발송
     * profileIds: 푸시를 받을 대상자 목록
     */
    @Transactional
    fun sendNotification(
        profileIds: List<String>,
        title: String,
        body: String,
        type: NotificationType,
        targetId: String? = null
    ) {
        if (profileIds.isEmpty()) return

        val notifications = mutableListOf<NotificationEntity>()
        val pushMessages = mutableListOf<Map<String, Any>>()

        for (profileId in profileIds) {
            // 1. DB에 알림 히스토리 적재
            val noti = NotificationEntity(
                notificationId = UUID.randomUUID().toString(),
                profileId = profileId,
                title = title,
                body = body,
                type = type,
                targetId = targetId
            )
            notifications.add(noti)

            // 2. 해당 유저의 디바이스 토큰 조회 후 푸시 메시지 구성
            val tokens = deviceTokenRepository.findByProfileId(profileId)
            for (token in tokens) {
                pushMessages.add(mapOf(
                    "to" to token.expoPushToken,
                    "title" to title,
                    "body" to body,
                    "data" to mapOf("targetId" to targetId, "type" to type.name)
                ))
            }
        }

        notificationRepository.saveAll(notifications)

        // 3. Expo 서버로 일괄 비동기 발송
        if (pushMessages.isNotEmpty()) {
            val webClient = webClientBuilder.baseUrl(expoPushUrl).build()
            
            webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pushMessages)
                .retrieve()
                .bodyToMono(String::class.java)
                .subscribe(
                    { response -> log.info("Expo Push Success: \$response") },
                    { error: Throwable -> log.error("Expo Push Failed: \${error.message}") }
                )
        }
    }
}
