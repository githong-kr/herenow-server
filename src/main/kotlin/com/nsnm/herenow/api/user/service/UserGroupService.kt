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
    private val profileRepository: ProfileRepository,
    private val notificationService: com.nsnm.herenow.api.notification.service.NotificationService
) : BaseService() {

    @Transactional
    fun createGroup(groupName: String, profileId: String): UserGroupDto {
        // 1. 그룹 생성 및 초기 초대 코드 발급
        val initialInviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).uppercase()
        var group = UserGroupEntity(
            groupName = groupName,
            ownerProfileId = profileId,
            inviteCode = initialInviteCode
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
    fun getGroupByInviteCode(inviteCode: String): UserGroupDto {
        val group = userGroupRepository.findByInviteCode(inviteCode)
            ?: throw BizException("유효하지 않거나 만료된 초대 코드입니다.")
        
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

                // 알림 발송 (해당 그룹 멤버 전체, 대상자 제외)
                val targetProfileIds = userGroupMemberRepository.findByGroupId(group.groupId)
                    .filter { it.profileId != request.profileId }
                    .map { it.profileId }
                val newMemberName = profileRepository.findById(request.profileId).orElse(null)?.name ?: "새 멤버"
                notificationService.sendNotification(
                    profileIds = targetProfileIds,
                    title = "스페이스 멤버 참여",
                    body = "${newMemberName}님이 '${group.groupName}' 스페이스에 합류했어요.",
                    type = com.nsnm.herenow.lib.model.entity.NotificationType.GROUP_MEMBER_JOINED,
                    targetId = group.groupId
                )
            }
        } else {
            request.status = JoinRequestStatus.REJECTED
        }

        groupJoinRequestRepository.save(request)
    }

    @Transactional
    fun updateGroupName(groupId: String, profileId: String, newName: String): UserGroupDto {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { BizException("존재하지 않는 그룹입니다.") }

        if (group.ownerProfileId != profileId) {
            throw BizException("그룹 소유자만 스페이스 이름을 변경할 수 있습니다.")
        }

        group.groupName = newName
        userGroupRepository.save(group)

        return UserGroupDto(
            groupId = group.groupId,
            groupName = group.groupName,
            ownerProfileId = group.ownerProfileId,
            inviteCode = group.inviteCode
        )
    }

    @Transactional
    fun removeGroupMember(groupId: String, requesterProfileId: String, targetProfileId: String) {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { BizException("존재하지 않는 스페이스입니다.") }

        if (group.ownerProfileId != requesterProfileId) {
            throw BizException("그룹 소유자만 멤버를 강퇴할 수 있습니다.")
        }

        if (group.ownerProfileId == targetProfileId) {
            throw BizException("소유자 스스로를 스페이스에서 강퇴할 수 없습니다.")
        }

        val member = userGroupMemberRepository.findByProfileIdAndGroupId(targetProfileId, groupId)
            ?: throw BizException("해당 스페이스의 멤버가 아닙니다.")

        val memberName = profileRepository.findById(targetProfileId).orElse(null)?.name ?: "멤버"

        userGroupMemberRepository.delete(member)

        // 알림 발송 (해당 그룹 멤버 전체, 대상자 제외)
        val targetProfileIds = userGroupMemberRepository.findByGroupId(group.groupId)
            .filter { it.profileId != targetProfileId }
            .map { it.profileId }
        notificationService.sendNotification(
            profileIds = targetProfileIds,
            title = "스페이스 멤버 퇴장",
            body = "${memberName}님이 '${group.groupName}' 스페이스에서 나갔습니다.",
            type = com.nsnm.herenow.lib.model.entity.NotificationType.GROUP_MEMBER_REMOVED,
            targetId = group.groupId
        )
    }

    @Transactional
    fun delegateOwner(groupId: String, requesterProfileId: String, targetProfileId: String) {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { BizException("존재하지 않는 스페이스입니다.") }

        if (group.ownerProfileId != requesterProfileId) {
            throw BizException("그룹 소유자만 방장 권한을 위임할 수 있습니다.")
        }

        if (group.ownerProfileId == targetProfileId) {
            throw BizException("이미 방장입니다.")
        }

        val currentOwnerMember = userGroupMemberRepository.findByProfileIdAndGroupId(requesterProfileId, groupId)
            ?: throw BizException("현재 멤버 정보를 찾을 수 없습니다.")

        val targetMember = userGroupMemberRepository.findByProfileIdAndGroupId(targetProfileId, groupId)
            ?: throw BizException("위임 대상 멤버가 스페이스에 존재하지 않습니다.")

        // 1. 소유권 이전
        group.ownerProfileId = targetProfileId
        userGroupRepository.save(group)

        // 2. 권한 변경 (기존 OWNER -> MEMBER, 대상 MEMBER -> OWNER)
        currentOwnerMember.role = GroupRole.MEMBER
        targetMember.role = GroupRole.OWNER

        userGroupMemberRepository.saveAll(listOf(currentOwnerMember, targetMember))
    }
}
