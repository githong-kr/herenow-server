package com.nsnm.herenow.api.item.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nsnm.herenow.api.item.dto.*
import com.nsnm.herenow.domain.item.entity.ItemEntity
import com.nsnm.herenow.domain.item.repository.ItemRepository
import com.nsnm.herenow.domain.space.repository.SpaceMemberRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val spaceMemberRepository: SpaceMemberRepository
) {

    private val mapper = jacksonObjectMapper()

    fun getItems(userId: UUID, spaceId: UUID): List<ItemResponse> {
        requireMembership(userId, spaceId)
        return itemRepository.findBySpaceId(spaceId).map { it.toResponse() }
    }

    @Transactional
    fun createItem(userId: UUID, spaceId: UUID, req: CreateItemRequest): ItemResponse {
        requireMembership(userId, spaceId)
        val item = itemRepository.save(ItemEntity(
            spaceId = spaceId, name = req.name, icon = req.icon,
            photoUrl = req.photoUrl, categoryId = req.categoryId,
            quantity = req.quantity, minQuantity = req.minQuantity,
            expiryDate = req.expiryDate, memo = req.memo,
            tags = mapper.writeValueAsString(req.tags),
            storageId = req.storageId, rowPos = req.rowPos, colPos = req.colPos
        ))
        return item.toResponse()
    }

    @Transactional
    fun updateItem(userId: UUID, itemId: UUID, req: UpdateItemRequest): ItemResponse {
        val item = itemRepository.findById(itemId).orElseThrow { notFound("Item") }
        requireMembership(userId, item.spaceId)
        req.name?.let { item.name = it }
        req.icon?.let { item.icon = it }
        req.photoUrl?.let { item.photoUrl = it }
        req.categoryId?.let { item.categoryId = it }
        req.quantity?.let { item.quantity = it }
        req.minQuantity?.let { item.minQuantity = it }
        req.expiryDate?.let { item.expiryDate = it }
        req.memo?.let { item.memo = it }
        req.tags?.let { item.tags = mapper.writeValueAsString(it) }
        item.updatedAt = OffsetDateTime.now()
        itemRepository.save(item)
        return item.toResponse()
    }

    @Transactional
    fun deleteItem(userId: UUID, itemId: UUID) {
        val item = itemRepository.findById(itemId).orElseThrow { notFound("Item") }
        requireMembership(userId, item.spaceId)
        itemRepository.delete(item)
    }

    @Transactional
    fun assignItem(userId: UUID, itemId: UUID, req: AssignItemRequest): ItemResponse {
        val item = itemRepository.findById(itemId).orElseThrow { notFound("Item") }
        requireMembership(userId, item.spaceId)
        item.storageId = req.storageId
        item.rowPos = req.rowPos
        item.colPos = req.colPos
        item.updatedAt = OffsetDateTime.now()
        itemRepository.save(item)
        return item.toResponse()
    }

    private fun requireMembership(userId: UUID, spaceId: UUID) {
        if (!spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "스페이스 멤버가 아닙니다.")
    }

    private fun notFound(entity: String) = ResponseStatusException(HttpStatus.NOT_FOUND, "$entity 를 찾을 수 없습니다.")
}
