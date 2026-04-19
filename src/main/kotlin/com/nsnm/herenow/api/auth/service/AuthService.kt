package com.nsnm.herenow.api.auth.service

import com.nsnm.herenow.api.auth.dto.InitRequest
import com.nsnm.herenow.api.auth.dto.InitResponse
import com.nsnm.herenow.domain.space.entity.SpaceEntity
import com.nsnm.herenow.domain.space.entity.SpaceMemberEntity
import com.nsnm.herenow.domain.space.repository.SpaceMemberRepository
import com.nsnm.herenow.domain.space.repository.SpaceRepository
import com.nsnm.herenow.domain.user.entity.ProfileEntity
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthService(
    private val profileRepository: ProfileRepository,
    private val spaceRepository: SpaceRepository,
    private val spaceMemberRepository: SpaceMemberRepository
) {

    @Transactional
    fun initUser(userId: UUID, req: InitRequest): InitResponse {
        // 1. 프로필 upsert
        val profile = profileRepository.findById(userId).orElse(null)
            ?: profileRepository.save(ProfileEntity(
                id = userId,
                name = req.name,
                avatarUrl = req.avatarUrl,
                marketingConsent = req.marketingConsent
            ))

        // 2. 기존 스페이스 확인 → 없으면 기본 스페이스 생성
        val existingMemberships = spaceMemberRepository.findByUserId(userId)
        val space = if (existingMemberships.isEmpty()) {
            val newSpace = spaceRepository.save(SpaceEntity(
                name = "우리집",
                ownerId = userId,
                inviteCode = generateInviteCode()
            ))
            spaceMemberRepository.save(SpaceMemberEntity(
                spaceId = newSpace.id,
                userId = userId,
                role = "OWNER"
            ))
            // 프로필에 기본 스페이스 설정
            profile.defaultSpaceId = newSpace.id
            profileRepository.save(profile)
            newSpace
        } else {
            spaceRepository.findById(existingMemberships.first().spaceId).orElseThrow()
        }

        return InitResponse(
            profileId = profile.id,
            spaceId = space.id,
            spaceName = space.name
        )
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
}
