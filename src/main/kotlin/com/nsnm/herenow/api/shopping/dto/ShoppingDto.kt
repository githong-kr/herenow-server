package com.nsnm.herenow.api.shopping.dto

import java.time.OffsetDateTime
import java.util.UUID

data class CreateShoppingItemRequest(val name: String)

data class ShoppingItemResponse(
    val id: UUID,
    val spaceId: UUID,
    val name: String,
    val checked: Boolean,
    val createdAt: OffsetDateTime
)
