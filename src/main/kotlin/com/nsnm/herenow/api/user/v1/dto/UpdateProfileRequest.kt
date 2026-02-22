package com.nsnm.herenow.api.user.v1.dto

import io.swagger.v3.oas.annotations.media.Schema

data class UpdateProfileRequest(
    @Schema(description = "변경할 닉네임 (이름)", example = "김제이")
    val name: String,
    
    @Schema(description = "변경할 프로필 아바타 이미지 URL (nullable)", example = "https://...")
    val avatarUrl: String? = null
)
