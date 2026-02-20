package com.nsnm.herenow.api.item.v1.dto

// Category DTOs
data class CategoryDto(
    val categoryId: String,
    val categoryName: String,
    val iconName: String?,
    val parentCategoryId: String?,
    val displayOrder: Int
)

data class CreateCategoryRequest(
    val categoryName: String,
    val iconName: String? = null,
    val parentCategoryId: String? = null,
    val displayOrder: Int = 0
)

// Location DTOs
data class LocationDto(
    val locationId: String,
    val locationName: String,
    val iconName: String?,
    val parentLocationId: String?,
    val displayOrder: Int
)

data class CreateLocationRequest(
    val locationName: String,
    val iconName: String? = null,
    val parentLocationId: String? = null,
    val displayOrder: Int = 0
)

// Tag DTOs
data class TagDto(
    val tagId: String,
    val tagName: String
)

data class CreateTagRequest(
    val tagName: String
)
