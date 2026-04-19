package com.nsnm.herenow.api.notification.controller

import com.nsnm.herenow.api.notification.dto.NotificationResponse
import com.nsnm.herenow.api.notification.service.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    fun getNotifications(principal: Principal): ResponseEntity<List<NotificationResponse>> {
        return ResponseEntity.ok(notificationService.getNotifications(UUID.fromString(principal.name)))
    }

    @PutMapping("/{id}/read")
    fun markAsRead(@PathVariable id: UUID, principal: Principal): ResponseEntity<NotificationResponse> {
        return ResponseEntity.ok(notificationService.markAsRead(UUID.fromString(principal.name), id))
    }
}
