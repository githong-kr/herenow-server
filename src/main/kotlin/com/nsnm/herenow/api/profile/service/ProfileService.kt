package com.nsnm.herenow.api.profile.service

import com.nsnm.herenow.api.profile.dto.*
import com.nsnm.herenow.domain.user.entity.ProfileEntity
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ProfileService(
    private val profileRepository: ProfileRepository
) {

    fun getProfile(userId: UUID): ProfileResponse {
        val profile = profileRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다.") }
        return profile.toResponse()
    }

    @Transactional
    fun updateProfile(userId: UUID, req: UpdateProfileRequest): ProfileResponse {
        val profile = profileRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다.") }
        req.name?.let { profile.name = it }
        req.avatarUrl?.let { profile.avatarUrl = it }
        req.defaultSpaceId?.let { profile.defaultSpaceId = it }
        req.marketingConsent?.let { profile.marketingConsent = it }
        profile.updatedAt = OffsetDateTime.now()
        profileRepository.save(profile)
        return profile.toResponse()
    }
}
