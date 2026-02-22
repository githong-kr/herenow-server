package com.nsnm.herenow.api.item.v1.dto

import io.swagger.v3.oas.annotations.Parameter
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
data class SearchItemRequest(
    @Parameter(description = "검색어 (아이템 이름)")
    val keyword: String? = null,

    @Parameter(description = "카테고리 ID")
    val categoryId: String? = null,

    @Parameter(description = "보관장소 ID")
    val locationId: String? = null,

    @Parameter(description = "상태 필터 (ALL, IMMINENT: 임박, EXPIRED: 만료)")
    val status: String? = null
)
