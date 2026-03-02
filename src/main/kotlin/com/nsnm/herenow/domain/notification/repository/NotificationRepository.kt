package com.nsnm.herenow.domain.notification.repository

import com.nsnm.herenow.lib.model.entity.NotificationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface NotificationRepository : JpaRepository<NotificationEntity, String> {
    fun findByProfileIdOrderByFrstRegTmstDesc(profileId: String, pageable: Pageable): Page<NotificationEntity>
    
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.profileId = :profileId AND n.isRead = false")
    fun markAllAsReadByProfileId(profileId: String)
}
