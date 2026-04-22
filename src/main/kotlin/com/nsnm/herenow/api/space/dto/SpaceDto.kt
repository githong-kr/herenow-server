package com.nsnm.herenow.api.space.dto

import java.time.OffsetDateTime
import java.util.UUID
import com.nsnm.herenow.domain.space.entity.SpaceEntity
import com.nsnm.herenow.domain.space.entity.SpaceMemberEntity
import com.nsnm.herenow.domain.space.entity.SpaceJoinRequestEntity
import com.nsnm.herenow.domain.user.entity.ProfileEntity

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

fun SpaceEntity.toResponse(role: String, memberCount: Int) = SpaceResponse(
    id = id, name = name, inviteCode = inviteCode,
    role = role, memberCount = memberCount, createdAt = createdAt
)

fun SpaceMemberEntity.toResponse(profile: ProfileEntity?) = SpaceMemberResponse(
    id = id, userId = userId,
    name = profile?.name, avatarUrl = profile?.avatarUrl,
    role = role, joinedAt = joinedAt
)

fun SpaceJoinRequestEntity.toResponse(profile: ProfileEntity?) = JoinRequestResponse(
    id = id, userId = userId,
    userName = profile?.name, avatarUrl = profile?.avatarUrl,
    inviteCodeUsed = inviteCodeUsed, status = status, createdAt = createdAt
)
