package com.nsnm.herenow.api.user.service

import com.nsnm.herenow.api.user.v1.dto.MyGroupDto
import com.nsnm.herenow.api.user.v1.dto.UserRegistrationRequest
import com.nsnm.herenow.api.user.v1.dto.UserRegistrationResponse
import com.nsnm.herenow.domain.group.model.entity.UserGroupEntity
import com.nsnm.herenow.domain.group.model.entity.UserGroupMemberEntity
import com.nsnm.herenow.domain.group.model.enums.GroupRole
import com.nsnm.herenow.domain.group.repository.UserGroupMemberRepository
import com.nsnm.herenow.domain.group.repository.UserGroupRepository
import com.nsnm.herenow.domain.user.model.entity.ProfileEntity
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import com.nsnm.herenow.fwk.core.error.BizException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.nsnm.herenow.fwk.core.base.BaseService

@Service
class UserService(
    private val profileRepository: ProfileRepository,
    private val userGroupRepository: UserGroupRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository
) : BaseService() {

    /**
     * 회원 가입 및 초기 그룹 생성 (단일 트랜잭션)
     */
    @Transactional
    fun registerInitialUser(uid: String, request: UserRegistrationRequest): UserRegistrationResponse {
        // 1. 이미 프로필이 존재하는지 체크
        if (profileRepository.existsById(uid)) {
            // 원칙적으로 에러 또는 존재하는 정보 리턴
            val existingProfile = profileRepository.findById(uid).orElseThrow()
            return UserRegistrationResponse(
                profileId = existingProfile.profileId,
                name = existingProfile.name,
                groupId = existingProfile.representativeGroupId ?: "UNKNOWN",
                groupName = "Existing Group"
            )
        }

        val userName = request.name ?: "Anonymous"

        // 2. Profile 생성
        var profile = ProfileEntity(
            profileId = uid,
            name = userName,
            marketingConsent = request.marketingConsent,
            avatarUrl = request.avatarUrl
        )
        profile = profileRepository.save(profile)

        // 3. User Group 생성 (Supabase 로직: <Name>'s Place)
        val groupName = "${userName}'s Place"
        var group = UserGroupEntity(
            groupName = groupName,
            ownerProfileId = uid
        )
        group = userGroupRepository.save(group)

        // 4. User Group Member (OWNER) 등록
        val groupMember = UserGroupMemberEntity(
            groupId = group.groupId,
            profileId = uid,
            role = GroupRole.OWNER
        )
        userGroupMemberRepository.save(groupMember)

        // 5. 대표 그룹 지정
        profile.representativeGroupId = group.groupId
        
        // Transactional 로 묶여있어 종료 시점에 영속성 컨텍스트(Dirty Checking)에 의해 profile 은 DB 자동 반영

        return UserRegistrationResponse(
            profileId = profile.profileId,
            name = profile.name,
            groupId = group.groupId,
            groupName = group.groupName
        )
    }

    /**
     * 사용자가 속한 모든 그룹 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getMyGroups(profileId: String): List<MyGroupDto> {
        val profile = profileRepository.findById(profileId)
            .orElseThrow { BizException("존재하지 않는 사용자입니다.") }

        val members = userGroupMemberRepository.findByProfileId(profileId)
        return members.mapNotNull { member ->
            val group = userGroupRepository.findById(member.groupId).orElse(null)
            if (group != null) {
                MyGroupDto(
                    groupId = group.groupId,
                    groupName = group.groupName,
                    role = member.role,
                    isDefault = (profile.representativeGroupId == group.groupId)
                )
            } else {
                null
            }
        }
    }

    /**
     * 사용자의 기본(대표) 그룹을 변경합니다.
     */
    @Transactional
    fun setDefaultGroup(profileId: String, groupId: String) {
        val profile = profileRepository.findById(profileId)
            .orElseThrow { BizException("존재하지 않는 사용자입니다.") }
        
        // 사용자가 해당 그룹 멤버인지 확인
        val member = userGroupMemberRepository.findByProfileIdAndGroupId(profileId, groupId)
            ?: throw BizException("해당 그룹의 멤버가 아닙니다.")
        
        profile.representativeGroupId = groupId
        profileRepository.save(profile)
    }
}
