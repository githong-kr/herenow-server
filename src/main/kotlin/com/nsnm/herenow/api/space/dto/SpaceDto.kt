package com.nsnm.herenow.api.space.dto

import java.time.OffsetDateTime
import java.util.UUID

// ─── Request DTOs ─────────────────────────────────────

data class CreateSpaceRequest(
    val name: String
)

data class UpdateSpaceRequest(
    val name: String
)

data class JoinSpaceRequest(
    val inviteCode: String
)

data class ProcessJoinRequestBody(
    val approve: Boolean
)

// ─── Response DTOs ────────────────────────────────────

data class SpaceResponse(
    val id: UUID,
    val name: String,
    val inviteCode: String?,
    val role: String,
    val memberCount: Int,
    val createdAt: OffsetDateTime
)

data class SpaceMemberResponse(
    val id: UUID,
    val userId: UUID,
    val name: String?,
    val avatarUrl: String?,
    val role: String,
    val joinedAt: OffsetDateTime
)

data class JoinRequestResponse(
    val id: UUID,
    val userId: UUID,
    val userName: String?,
    val avatarUrl: String?,
    val inviteCodeUsed: String?,
    val status: String,
    val createdAt: OffsetDateTime
)
