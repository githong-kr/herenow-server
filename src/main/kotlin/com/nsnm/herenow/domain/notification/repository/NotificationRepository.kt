package com.nsnm.herenow.domain.notification.repository

import com.nsnm.herenow.domain.notification.entity.NotificationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationRepository : JpaRepository<NotificationEntity, UUID> {
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<NotificationEntity>
    fun findByUserIdAndIsReadFalse(userId: UUID): List<NotificationEntity>
}
