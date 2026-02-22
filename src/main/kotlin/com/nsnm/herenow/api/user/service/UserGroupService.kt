package com.nsnm.herenow.api.user.service

import com.nsnm.herenow.api.user.v1.dto.GroupJoinRequestDto
import com.nsnm.herenow.api.user.v1.dto.GroupMemberDto
import com.nsnm.herenow.api.user.v1.dto.UserGroupDto
import com.nsnm.herenow.domain.group.model.entity.GroupJoinRequestEntity
import com.nsnm.herenow.domain.group.model.entity.UserGroupEntity
import com.nsnm.herenow.domain.group.model.entity.UserGroupMemberEntity
import com.nsnm.herenow.domain.group.model.enums.GroupRole
import com.nsnm.herenow.domain.group.model.enums.JoinRequestStatus
import com.nsnm.herenow.domain.group.repository.GroupJoinRequestRepository
import com.nsnm.herenow.domain.group.repository.UserGroupMemberRepository
import com.nsnm.herenow.domain.group.repository.UserGroupRepository
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import com.nsnm.herenow.fwk.core.error.BizException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import com.nsnm.herenow.fwk.core.base.BaseService

@Service
class UserGroupService(
    private val userGroupRepository: UserGroupRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository,
    private val groupJoinRequestRepository: GroupJoinRequestRepository,
    private val profileRepository: ProfileRepository
) : BaseService() {

    @Transactional
    fun createGroup(groupName: String, profileId: String): UserGroupDto {
        // 1. 그룹 생성
        var group = UserGroupEntity(
            groupName = groupName,
            ownerProfileId = profileId
        )
        group = userGroupRepository.save(group)

        // 2. 그룹 소유자를 멤버로 등록
        val groupMember = UserGroupMemberEntity(
            groupId = group.groupId,
            profileId = profileId,
            role = GroupRole.OWNER
        )
        userGroupMemberRepository.save(groupMember)

        return UserGroupDto(
            groupId = group.groupId,
            groupName = group.groupName,
            ownerProfileId = group.ownerProfileId,
            inviteCode = group.inviteCode
        )
    }

    @Transactional(readOnly = true)
    fun getGroupInfo(groupId: String): UserGroupDto {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { BizException("존재하지 않는 그룹입니다.") }
        
        return UserGroupDto(
            groupId = group.groupId,
            groupName = group.groupName,
            ownerProfileId = group.ownerProfileId,
            inviteCode = group.inviteCode
        )
    }

    @Transactional(readOnly = true)
    fun getGroupMembers(groupId: String): List<GroupMemberDto> {
        val members = userGroupMemberRepository.findByGroupId(groupId)
        return members.map {
            val profile = profileRepository.findById(it.profileId).orElse(null)
            GroupMemberDto(
                groupMemberId = it.groupMemberId,
                profileId = it.profileId,
                name = profile?.name ?: "알 수 없음",
                avatarUrl = profile?.avatarUrl,
                role = it.role
            )
        }
    }

    @Transactional
    fun refreshInviteCode(groupId: String, profileId: String): String {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { BizException("존재하지 않는 그룹입니다.") }

        // 현재 요청자가 이 그룹의 OWNER 인지 검증
        if (group.ownerProfileId != profileId) {
            throw BizException("그룹 소유자만 초대 코드를 발급할 수 있습니다.")
        }

        // 짧은 랜덤 코드(예: 8자리 영문 숫자) 생성 (여기선 UUID 가공)
        val newCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).uppercase()
        group.inviteCode = newCode
        
        userGroupRepository.save(group)
        return newCode
    }

    @Transactional
    fun joinGroupByInviteCode(inviteCode: String, profileId: String): GroupJoinRequestDto {
        val group = userGroupRepository.findByInviteCode(inviteCode)
            ?: throw BizException("유효하지 않거나 만료된 초대 코드입니다.")

        // 이미 멤버로 등록되어 있는지 확인
        val existingMember = userGroupMemberRepository.findByProfileIdAndGroupId(profileId, group.groupId)
        if (existingMember != null) {
            throw BizException("이미 해당 그룹의 멤버입니다.")
        }

        // 이미 대기 중인 요청이 있는지 확인
        val existingRequest = groupJoinRequestRepository.findByGroupIdAndProfileIdAndStatus(group.groupId, profileId, JoinRequestStatus.PENDING)
        if (existingRequest != null) {
            throw BizException("이미 가입 승인을 대기 중입니다.")
        }

        // 새 대기 요청 생성
        val newRequest = GroupJoinRequestEntity(
            groupId = group.groupId,
            profileId = profileId,
            inviteCodeUsed = inviteCode
        )
        groupJoinRequestRepository.save(newRequest)

        val profile = profileRepository.findById(profileId).orElse(null)

        return GroupJoinRequestDto(
            requestId = newRequest.requestId,
            groupId = newRequest.groupId,
            profileId = newRequest.profileId,
            name = profile?.name ?: "알 수 없음",
            avatarUrl = profile?.avatarUrl,
            inviteCodeUsed = newRequest.inviteCodeUsed,
            status = newRequest.status.name
        )
    }

    @Transactional(readOnly = true)
    fun getPendingJoinRequests(groupId: String, profileId: String): List<GroupJoinRequestDto> {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { BizException("존재하지 않는 그룹입니다.") }

        if (group.ownerProfileId != profileId) {
            throw BizException("권한이 없습니다.")
        }

        val requests = groupJoinRequestRepository.findByGroupIdAndStatus(groupId, JoinRequestStatus.PENDING)
        return requests.map {
            val profile = profileRepository.findById(it.profileId).orElse(null)
            GroupJoinRequestDto(
                requestId = it.requestId,
                groupId = it.groupId,
                profileId = it.profileId,
                name = profile?.name ?: "알 수 없음",
                avatarUrl = profile?.avatarUrl,
                inviteCodeUsed = it.inviteCodeUsed,
                status = it.status.name
            )
        }
    }

    @Transactional
    fun processJoinRequest(groupId: String, requestId: String, profileId: String, approve: Boolean) {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { BizException("존재하지 않는 그룹입니다.") }

        if (group.ownerProfileId != profileId) {
            throw BizException("그룹 소유자만 가입 승인/거절을 할 수 있습니다.")
        }

        val request = groupJoinRequestRepository.findById(requestId)
            .orElseThrow { BizException("존재하지 않는 가입 요청입니다.") }

        if (request.groupId != groupId || request.status != JoinRequestStatus.PENDING) {
            throw BizException("유효하지 않은 가입 요청 상태입니다.")
        }

        if (approve) {
            request.status = JoinRequestStatus.APPROVED

            val existingMember = userGroupMemberRepository.findByProfileIdAndGroupId(request.profileId, group.groupId)
            if (existingMember == null) {
                val newMember = UserGroupMemberEntity(
                    groupId = group.groupId,
                    profileId = request.profileId,
                    role = GroupRole.MEMBER
                )
                userGroupMemberRepository.save(newMember)
            }
        } else {
            request.status = JoinRequestStatus.REJECTED
        }

        groupJoinRequestRepository.save(request)
    }
}
