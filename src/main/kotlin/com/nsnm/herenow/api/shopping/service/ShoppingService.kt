package com.nsnm.herenow.api.shopping.service

import com.nsnm.herenow.api.shopping.dto.*
import com.nsnm.herenow.domain.shopping.entity.ShoppingListEntity
import com.nsnm.herenow.domain.shopping.repository.ShoppingListRepository
import com.nsnm.herenow.domain.space.repository.SpaceMemberRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class ShoppingService(
    private val shoppingListRepository: ShoppingListRepository,
    private val spaceMemberRepository: SpaceMemberRepository
) {

    fun getShoppingList(userId: UUID, spaceId: UUID): List<ShoppingItemResponse> {
        requireMembership(userId, spaceId)
        return shoppingListRepository.findBySpaceIdOrderByCreatedAtDesc(spaceId).map { it.toResponse() }
    }

    @Transactional
    fun addItem(userId: UUID, spaceId: UUID, req: CreateShoppingItemRequest): ShoppingItemResponse {
        requireMembership(userId, spaceId)
        val item = shoppingListRepository.save(ShoppingListEntity(spaceId = spaceId, name = req.name))
        return item.toResponse()
    }

    @Transactional
    fun toggleItem(userId: UUID, id: UUID): ShoppingItemResponse {
        val item = shoppingListRepository.findById(id).orElseThrow { notFound("ShoppingItem") }
        requireMembership(userId, item.spaceId)
        item.checked = !item.checked
        shoppingListRepository.save(item)
        return item.toResponse()
    }

    @Transactional
    fun deleteItem(userId: UUID, id: UUID) {
        val item = shoppingListRepository.findById(id).orElseThrow { notFound("ShoppingItem") }
        requireMembership(userId, item.spaceId)
        shoppingListRepository.delete(item)
    }

    @Transactional
    fun clearPurchased(userId: UUID, spaceId: UUID) {
        requireMembership(userId, spaceId)
        shoppingListRepository.deleteBySpaceIdAndCheckedTrue(spaceId)
    }

    private fun requireMembership(userId: UUID, spaceId: UUID) {
        if (!spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "스페이스 멤버가 아닙니다.")
    }

    private fun notFound(entity: String) = ResponseStatusException(HttpStatus.NOT_FOUND, "$entity 를 찾을 수 없습니다.")
}
