package com.nsnm.herenow.api.room.dto

import java.time.OffsetDateTime
import java.util.UUID

data class CreateRoomRequest(val name: String, val icon: String = "Home", val color: String = "bg-emerald-500")
data class UpdateRoomRequest(val name: String? = null, val icon: String? = null, val color: String? = null)
data class ReorderRoomsRequest(val roomIds: List<UUID>)

data class RoomResponse(
    val id: UUID,
    val spaceId: UUID,
    val name: String,
    val icon: String,
    val color: String,
    val displayOrder: Int,
    val createdAt: OffsetDateTime
)
