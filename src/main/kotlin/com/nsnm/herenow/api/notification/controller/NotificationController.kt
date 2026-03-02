package com.nsnm.herenow.api.notification.controller

import com.nsnm.herenow.api.notification.dto.DeviceTokenRequest
import com.nsnm.herenow.api.notification.dto.NotificationResponse
import com.nsnm.herenow.api.notification.service.NotificationService
import com.nsnm.herenow.fwk.core.base.BaseController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Notifications", description = "푸시 토큰 및 알림 센터(Inbox) API")
@RestController
@RequestMapping("/api/v1/users/me")
class NotificationController(
    private val notificationService: NotificationService
) : BaseController() {

    @Operation(summary = "푸시 토큰 등록", description = "Expo 클라이언트에서 획득한 기기의 푸시 토큰을 서버에 등록합니다.")
    @PostMapping("/push-tokens")
    fun registerDeviceToken(@RequestBody request: DeviceTokenRequest) {
        val uid = SecurityContextHolder.getContext().authentication.name
        notificationService.registerDeviceToken(uid, request)
    }

    @Operation(summary = "내 알림함 목록 조회", description = "푸시 수신 내역(알림 센터)을 페이징하여 최신순으로 반환합니다.")
    @GetMapping("/notifications")
    fun getMyNotifications(@ParameterObject pageable: Pageable): Page<NotificationResponse> {
        val uid = SecurityContextHolder.getContext().authentication.name
        return notificationService.getMyNotifications(uid, pageable)
    }

    @Operation(summary = "특정 알림 읽음 처리", description = "알림 1건을 읽음 상태로 업데이트합니다.")
    @PutMapping("/notifications/{id}/read")
    fun markAsRead(@PathVariable id: String) {
        notificationService.markAsRead(id)
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "현재 사용자의 모든 미확인 알림을 읽음 처리합니다.")
    @PutMapping("/notifications/read-all")
    fun markAllAsRead() {
        val uid = SecurityContextHolder.getContext().authentication.name
        notificationService.markAllAsRead(uid)
    }
}
