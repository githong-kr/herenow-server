package com.nsnm.herenow.api.storage.service

import com.nsnm.herenow.api.storage.dto.*
import com.nsnm.herenow.domain.room.repository.RoomRepository
import com.nsnm.herenow.domain.space.repository.SpaceMemberRepository
import com.nsnm.herenow.domain.storage.entity.StorageEntity
import com.nsnm.herenow.domain.storage.repository.StorageRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class StorageService(
    private val storageRepository: StorageRepository,
    private val roomRepository: RoomRepository,
    private val spaceMemberRepository: SpaceMemberRepository
) {

    fun getStorages(userId: UUID, roomId: UUID): List<StorageResponse> {
        val room = roomRepository.findById(roomId).orElseThrow { notFound("Room") }
        requireMembership(userId, room.spaceId)
        return storageRepository.findByRoomId(roomId).map { it.toResponse() }
    }

    @Transactional
    fun createStorage(userId: UUID, roomId: UUID, req: CreateStorageRequest): StorageResponse {
        val room = roomRepository.findById(roomId).orElseThrow { notFound("Room") }
        requireMembership(userId, room.spaceId)
        val storage = storageRepository.save(StorageEntity(
            roomId = roomId, name = req.name,
            x = req.x, y = req.y, w = req.w, h = req.h,
            color = req.color, topColor = req.topColor, design = req.design,
            gridRows = req.gridRows, gridCols = req.gridCols, layout = req.layout
        ))
        return storage.toResponse()
    }

    @Transactional
    fun updateStorage(userId: UUID, storageId: UUID, req: UpdateStorageRequest): StorageResponse {
        val storage = storageRepository.findById(storageId).orElseThrow { notFound("Storage") }
        val room = roomRepository.findById(storage.roomId).orElseThrow { notFound("Room") }
        requireMembership(userId, room.spaceId)
        req.name?.let { storage.name = it }
        req.x?.let { storage.x = it }
        req.y?.let { storage.y = it }
        req.w?.let { storage.w = it }
        req.h?.let { storage.h = it }
        req.color?.let { storage.color = it }
        req.topColor?.let { storage.topColor = it }
        req.design?.let { storage.design = it }
        req.gridRows?.let { storage.gridRows = it }
        req.gridCols?.let { storage.gridCols = it }
        req.layout?.let { storage.layout = it }
        storage.updatedAt = OffsetDateTime.now()
        storageRepository.save(storage)
        return storage.toResponse()
    }

    @Transactional
    fun deleteStorage(userId: UUID, storageId: UUID) {
        val storage = storageRepository.findById(storageId).orElseThrow { notFound("Storage") }
        val room = roomRepository.findById(storage.roomId).orElseThrow { notFound("Room") }
        requireMembership(userId, room.spaceId)
        storageRepository.delete(storage)
    }

    private fun requireMembership(userId: UUID, spaceId: UUID) {
        if (!spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "스페이스 멤버가 아닙니다.")
    }

    private fun notFound(entity: String) = ResponseStatusException(HttpStatus.NOT_FOUND, "$entity 를 찾을 수 없습니다.")

    private fun StorageEntity.toResponse() = StorageResponse(
        id = id, roomId = roomId, name = name,
        x = x, y = y, w = w, h = h,
        color = color, topColor = topColor, design = design,
        gridRows = gridRows, gridCols = gridCols, layout = layout,
        createdAt = createdAt
    )
}
