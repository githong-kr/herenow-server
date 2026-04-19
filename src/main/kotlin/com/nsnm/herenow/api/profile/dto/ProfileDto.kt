package com.nsnm.herenow.api.profile.dto

import java.util.UUID

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
