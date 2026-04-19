package com.nsnm.herenow.domain.room.repository

import com.nsnm.herenow.domain.room.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RoomRepository : JpaRepository<RoomEntity, UUID> {
    fun findBySpaceIdOrderByDisplayOrder(spaceId: UUID): List<RoomEntity>
}
