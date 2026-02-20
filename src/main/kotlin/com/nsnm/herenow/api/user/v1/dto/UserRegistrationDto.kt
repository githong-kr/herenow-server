package com.nsnm.herenow.api.user.v1.dto

data class UserRegistrationRequest(
    val name: String? = null,
    val avatarUrl: String? = null,
    val marketingConsent: Boolean = true
)

data class UserRegistrationResponse(
    val profileId: String,
    val name: String,
    val groupId: String,
    val groupName: String
)
