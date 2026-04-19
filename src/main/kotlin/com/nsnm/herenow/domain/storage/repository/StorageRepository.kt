package com.nsnm.herenow.domain.storage.repository

import com.nsnm.herenow.domain.storage.entity.StorageEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StorageRepository : JpaRepository<StorageEntity, UUID> {
    fun findByRoomId(roomId: UUID): List<StorageEntity>
    fun findByRoomIdIn(roomIds: List<UUID>): List<StorageEntity>
}
