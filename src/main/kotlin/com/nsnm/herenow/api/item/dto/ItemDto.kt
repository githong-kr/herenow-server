package com.nsnm.herenow.api.item.dto

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class CreateItemRequest(
    val name: String,
    val icon: String = "📦",
    val photoUrl: String? = null,
    val categoryId: String? = null,
    val quantity: Int = 1,
    val minQuantity: Int = 0,
    val expiryDate: LocalDate? = null,
    val memo: String = "",
    val tags: List<String> = emptyList(),
    val storageId: UUID? = null,
    val rowPos: Int? = null,
    val colPos: Int? = null
)

data class UpdateItemRequest(
    val name: String? = null,
    val icon: String? = null,
    val photoUrl: String? = null,
    val categoryId: String? = null,
    val quantity: Int? = null,
    val minQuantity: Int? = null,
    val expiryDate: LocalDate? = null,
    val memo: String? = null,
    val tags: List<String>? = null
)

data class AssignItemRequest(
    val storageId: UUID?,
    val rowPos: Int?,
    val colPos: Int?
)

data class ItemResponse(
    val id: UUID,
    val spaceId: UUID,
    val storageId: UUID?,
    val rowPos: Int?,
    val colPos: Int?,
    val name: String,
    val icon: String,
    val photoUrl: String?,
    val categoryId: String?,
    val quantity: Int,
    val minQuantity: Int,
    val expiryDate: LocalDate?,
    val memo: String?,
    val tags: List<String>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
