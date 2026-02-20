package com.nsnm.herenow.api.user.v1.dto

import com.nsnm.herenow.domain.group.model.enums.GroupRole

data class UserGroupDto(
    val groupId: String,
    val groupName: String,
    val ownerProfileId: String,
    val inviteCode: String?
)

data class GroupMemberDto(
    val groupMemberId: String,
    val profileId: String,
    val role: GroupRole
)

data class JoinGroupRequest(
    val inviteCode: String
)

data class GroupJoinRequestDto(
    val requestId: String,
    val groupId: String,
    val profileId: String,
    val inviteCodeUsed: String,
    val status: String
)

data class ProcessJoinRequest(
    val requestId: String,
    val approve: Boolean // true: 승인, false: 거절
)
