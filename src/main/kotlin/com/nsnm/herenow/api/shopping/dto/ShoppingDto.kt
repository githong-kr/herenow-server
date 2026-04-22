package com.nsnm.herenow.api.shopping.dto

import java.time.OffsetDateTime
import java.util.UUID
import com.nsnm.herenow.domain.shopping.entity.ShoppingListEntity

data class CreateShoppingItemRequest(val name: String)

data class ShoppingItemResponse(
    val id: UUID,
    val spaceId: UUID,
    val name: String,
    val checked: Boolean,
    val createdAt: OffsetDateTime
)

fun ShoppingListEntity.toResponse() = ShoppingItemResponse(
    id = id, spaceId = spaceId, name = name, checked = checked, createdAt = createdAt
)
