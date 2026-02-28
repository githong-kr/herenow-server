package com.nsnm.herenow.api.item.v1.dto

import io.swagger.v3.oas.annotations.media.Schema
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
    val shortcutNumber: Int? = null,
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
    val shortcutNumber: Int? = null,
    val tags: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList()
)

data class ItemResponse(
    val itemId: String,
    val itemName: String,
    val categoryId: String?,
    val categoryName: String?,
    val locationId: String?,
    val locationName: String?,
    val locationPhotoUrl: String? = null,
    val quantity: Int,
    val minQuantity: Int,
    val expiryDate: LocalDate?,
    val memo: String?,
    val shortcutNumber: Int? = null,
    val tags: List<String>,
    val photoUrls: List<String>,
    val frstRegTmst: java.time.LocalDateTime? = null,
    val frstRegName: String? = null,
    val lastChngTmst: java.time.LocalDateTime? = null,
    val lastChngName: String? = null
)

@Schema(description = "아이템 변경 이력 응답 객체")
data class ItemHistoryResponse(
    val itemHistoryId: String,
    val actionType: String,
    val title: String,
    val message: String,
    val details: List<String>,
    val tmst: String?
)
