package com.nsnm.herenow.api.profile.dto

import java.util.UUID
import com.nsnm.herenow.domain.user.entity.ProfileEntity

data class ProfileResponse(
    val id: UUID,
    val name: String?,
    val avatarUrl: String?,
    val defaultSpaceId: UUID?,
    val marketingConsent: Boolean
)

data class UpdateProfileRequest(
    val name: String? = null,
    val avatarUrl: String? = null,
    val defaultSpaceId: UUID? = null,
    val marketingConsent: Boolean? = null
)

fun ProfileEntity.toResponse() = ProfileResponse(
    id = id, name = name, avatarUrl = avatarUrl,
    defaultSpaceId = defaultSpaceId, marketingConsent = marketingConsent
)
