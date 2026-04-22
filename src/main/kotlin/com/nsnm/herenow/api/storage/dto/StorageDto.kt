package com.nsnm.herenow.api.storage.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID
import com.nsnm.herenow.domain.storage.entity.StorageEntity

data class CreateStorageRequest(
    val name: String,
    val x: BigDecimal = BigDecimal("10"),
    val y: BigDecimal = BigDecimal("10"),
    val w: BigDecimal = BigDecimal("25"),
    val h: BigDecimal = BigDecimal("25"),
    val color: String = "bg-slate-200",
    val topColor: String = "bg-slate-100",
    val design: String? = "drawer",
    val gridRows: Int = 3,
    val gridCols: Int = 3,
    val layout: String = "{}"
)

data class UpdateStorageRequest(
    val name: String? = null,
    val x: BigDecimal? = null,
    val y: BigDecimal? = null,
    val w: BigDecimal? = null,
    val h: BigDecimal? = null,
    val color: String? = null,
    val topColor: String? = null,
    val design: String? = null,
    val gridRows: Int? = null,
    val gridCols: Int? = null,
    val layout: String? = null
)

data class StorageResponse(
    val id: UUID,
    val roomId: UUID,
    val name: String,
    val x: BigDecimal,
    val y: BigDecimal,
    val w: BigDecimal,
    val h: BigDecimal,
    val color: String,
    val topColor: String,
    val design: String?,
    val gridRows: Int,
    val gridCols: Int,
    val layout: String,
    val createdAt: OffsetDateTime
)

fun StorageEntity.toResponse() = StorageResponse(
    id = id, roomId = roomId, name = name,
    x = x, y = y, w = w, h = h,
    color = color, topColor = topColor, design = design,
    gridRows = gridRows, gridCols = gridCols, layout = layout,
    createdAt = createdAt
)
