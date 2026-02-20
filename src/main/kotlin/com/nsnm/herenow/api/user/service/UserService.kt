package com.nsnm.herenow.api.user.service

import com.nsnm.herenow.api.user.v1.dto.UserRegistrationRequest
import com.nsnm.herenow.api.user.v1.dto.UserRegistrationResponse
import com.nsnm.herenow.domain.group.model.entity.UserGroupEntity
import com.nsnm.herenow.domain.group.model.entity.UserGroupMemberEntity
import com.nsnm.herenow.domain.group.model.enums.GroupRole
import com.nsnm.herenow.domain.group.repository.UserGroupMemberRepository
import com.nsnm.herenow.domain.group.repository.UserGroupRepository
import com.nsnm.herenow.domain.user.model.entity.ProfileEntity
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val profileRepository: ProfileRepository,
    private val userGroupRepository: UserGroupRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository
) {

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
}
