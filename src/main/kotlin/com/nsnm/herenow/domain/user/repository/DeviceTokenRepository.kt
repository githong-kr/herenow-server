package com.nsnm.herenow.domain.user.repository

import com.nsnm.herenow.lib.model.entity.DeviceTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceTokenRepository : JpaRepository<DeviceTokenEntity, String> {
    fun findByProfileId(profileId: String): List<DeviceTokenEntity>
    fun deleteByExpoPushToken(expoPushToken: String)
    fun existsByExpoPushToken(expoPushToken: String): Boolean
}
