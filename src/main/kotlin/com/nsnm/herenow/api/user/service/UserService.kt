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
                avatarUrl = existingProfile.avatarUrl,
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
            avatarUrl = profile.avatarUrl,
            groupId = group.groupId,
            groupName = group.groupName
        )
    }

    /**
     * 회원 프로필 수정 (닉네임, 사진 변경)
     */
    @Transactional
    fun updateProfile(profileId: String, request: com.nsnm.herenow.api.user.v1.dto.UpdateProfileRequest): UserRegistrationResponse {
        val profile = profileRepository.findById(profileId)
            .orElseThrow { BizException("존재하지 않는 사용자입니다.") }
        
        profile.name = request.name
        profile.avatarUrl = request.avatarUrl // 널러블 처리
        
        // 대표 그룹 정보 조회 (현재 화면용, 옵셔널)
        val groupName = profile.representativeGroupId?.let { 
            userGroupRepository.findById(it).orElse(null)?.groupName 
        } ?: "UNKNOWN"

        return UserRegistrationResponse(
            profileId = profile.profileId,
            name = profile.name,
            avatarUrl = profile.avatarUrl,
            groupId = profile.representativeGroupId ?: "UNKNOWN",
            groupName = groupName
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

    /**
     * 회원 탈퇴 (Hard Delete)
     * - 자신이 속한 모든 그룹의 멤버십을 해제/제거
     * - 방장(OWNER)인 스페이스 검사: 본인 외 다른 멤버가 있으면 예외 발생 (위임 필요), 혼자면 그룹 폭파
     * - 마지막으로 프로필 엔티티를 삭제
     */
    @Transactional
    fun withdrawAccount(profileId: String) {
        val members = userGroupMemberRepository.findByProfileId(profileId)

        for (member in members) {
            if (member.role == GroupRole.OWNER) {
                // 이 그룹에 다른 멤버가 있는지 확인
                val groupMembers = userGroupMemberRepository.findByGroupId(member.groupId)
                if (groupMembers.size > 1) {
                    val group = userGroupRepository.findById(member.groupId).orElse(null)
                    val groupName = group?.groupName ?: "알 수 없는 스페이스"
                    throw BizException("'$groupName'의 방장입니다. 다른 멤버에게 권한을 위임한 후 탈퇴해 주세요.")
                } else {
                    // 혼자 있는 그룹이면 그룹 자체를 삭제
                    userGroupMemberRepository.delete(member)
                    userGroupRepository.deleteById(member.groupId)
                    continue
                }
            }
            // MEMBER 인 경우 단순 탈퇴 (멤버십 삭제)
            userGroupMemberRepository.delete(member)
        }

        // 프로필(UserEntity) 파기
        profileRepository.deleteById(profileId)
    }
}
