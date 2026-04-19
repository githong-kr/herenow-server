package com.nsnm.herenow.api.room.service

import com.nsnm.herenow.api.room.dto.*
import com.nsnm.herenow.domain.room.entity.RoomEntity
import com.nsnm.herenow.domain.room.repository.RoomRepository
import com.nsnm.herenow.domain.space.repository.SpaceMemberRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val spaceMemberRepository: SpaceMemberRepository
) {

    fun getRooms(userId: UUID, spaceId: UUID): List<RoomResponse> {
        requireMembership(userId, spaceId)
        return roomRepository.findBySpaceIdOrderByDisplayOrder(spaceId).map { it.toResponse() }
    }

    @Transactional
    fun createRoom(userId: UUID, spaceId: UUID, req: CreateRoomRequest): RoomResponse {
        requireMembership(userId, spaceId)
        val maxOrder = roomRepository.findBySpaceIdOrderByDisplayOrder(spaceId).maxOfOrNull { it.displayOrder } ?: -1
        val room = roomRepository.save(RoomEntity(
            spaceId = spaceId, name = req.name, icon = req.icon, color = req.color, displayOrder = maxOrder + 1
        ))
        return room.toResponse()
    }

    @Transactional
    fun updateRoom(userId: UUID, roomId: UUID, req: UpdateRoomRequest): RoomResponse {
        val room = roomRepository.findById(roomId).orElseThrow { notFound("Room") }
        requireMembership(userId, room.spaceId)
        req.name?.let { room.name = it }
        req.icon?.let { room.icon = it }
        req.color?.let { room.color = it }
        room.updatedAt = OffsetDateTime.now()
        roomRepository.save(room)
        return room.toResponse()
    }

    @Transactional
    fun deleteRoom(userId: UUID, roomId: UUID) {
        val room = roomRepository.findById(roomId).orElseThrow { notFound("Room") }
        requireMembership(userId, room.spaceId)
        roomRepository.delete(room)
    }

    @Transactional
    fun reorderRooms(userId: UUID, spaceId: UUID, req: ReorderRoomsRequest) {
        requireMembership(userId, spaceId)
        req.roomIds.forEachIndexed { index, roomId ->
            roomRepository.findById(roomId).ifPresent { room ->
                room.displayOrder = index
                roomRepository.save(room)
            }
        }
    }

    private fun requireMembership(userId: UUID, spaceId: UUID) {
        if (!spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "스페이스 멤버가 아닙니다.")
    }

    private fun notFound(entity: String) = ResponseStatusException(HttpStatus.NOT_FOUND, "$entity 를 찾을 수 없습니다.")

    private fun RoomEntity.toResponse() = RoomResponse(
        id = id, spaceId = spaceId, name = name, icon = icon, color = color,
        displayOrder = displayOrder, createdAt = createdAt
    )
}
