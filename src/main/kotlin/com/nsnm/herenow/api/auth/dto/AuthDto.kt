package com.nsnm.herenow.api.auth.dto

import java.util.UUID

data class InitRequest(
    val name: String,
    val avatarUrl: String? = null,
    val marketingConsent: Boolean = false
)

data class InitResponse(
    val profileId: UUID,
    val spaceId: UUID,
    val spaceName: String
)
