package com.nsnm.herenow.domain.device.repository

import com.nsnm.herenow.domain.device.entity.DeviceTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeviceTokenRepository : JpaRepository<DeviceTokenEntity, UUID> {
    fun findByUserId(userId: UUID): List<DeviceTokenEntity>
    fun findByUserIdAndPushToken(userId: UUID, pushToken: String): DeviceTokenEntity?
    fun deleteByUserIdAndPushToken(userId: UUID, pushToken: String)
}
