package com.nsnm.herenow.api.item.v1.dto

import java.math.BigDecimal
import java.time.LocalDate

data class CreateItemRequest(
    val categoryId: String? = null,
    val locationId: String? = null,
    val itemName: String,
    val quantity: Int = 1,
    val minQuantity: Int = 0,
    val purchaseDate: LocalDate? = null,
    val purchasePlace: String? = null,
    val price: BigDecimal? = null,
    val expiryDate: LocalDate? = null,
    val memo: String? = null,
    val tags: List<String> = emptyList(), // 태그명 또는 ID 리스트
    val photoUrls: List<String> = emptyList() // 첨부될 사진 경로 리스트 
)

data class UpdateItemRequest(
    val categoryId: String? = null,
    val locationId: String? = null,
    val itemName: String,
    val quantity: Int = 1,
    val minQuantity: Int = 0,
    val purchaseDate: LocalDate? = null,
    val purchasePlace: String? = null,
    val price: BigDecimal? = null,
    val expiryDate: LocalDate? = null,
    val memo: String? = null,
    val tags: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList()
)

data class ItemResponse(
    val itemId: String,
    val itemName: String,
    val categoryId: String?,
    val locationId: String?,
    val quantity: Int,
    val tags: List<String>,
    val photoUrls: List<String>
)
