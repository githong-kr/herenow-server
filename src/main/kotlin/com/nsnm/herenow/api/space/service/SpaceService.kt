package com.nsnm.herenow.api.space.service

import com.nsnm.herenow.api.space.dto.*
import com.nsnm.herenow.domain.space.entity.SpaceEntity
import com.nsnm.herenow.domain.space.entity.SpaceJoinRequestEntity
import com.nsnm.herenow.domain.space.entity.SpaceMemberEntity
import com.nsnm.herenow.domain.space.repository.SpaceJoinRequestRepository
import com.nsnm.herenow.domain.space.repository.SpaceMemberRepository
import com.nsnm.herenow.domain.space.repository.SpaceRepository
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SpaceService(
    private val spaceRepository: SpaceRepository,
    private val spaceMemberRepository: SpaceMemberRepository,
    private val spaceJoinRequestRepository: SpaceJoinRequestRepository,
    private val profileRepository: ProfileRepository
) {

    fun getMySpaces(userId: UUID): List<SpaceResponse> {
        val memberships = spaceMemberRepository.findByUserId(userId)
        return memberships.mapNotNull { membership ->
            spaceRepository.findById(membership.spaceId).orElse(null)?.let { space ->
                val memberCount = spaceMemberRepository.findBySpaceId(space.id).size
                SpaceResponse(
                    id = space.id, name = space.name, inviteCode = space.inviteCode,
                    role = membership.role, memberCount = memberCount, createdAt = space.createdAt
                )
            }
        }
    }

    @Transactional
    fun createSpace(userId: UUID, req: CreateSpaceRequest): SpaceResponse {
        val space = spaceRepository.save(SpaceEntity(
            name = req.name, ownerId = userId, inviteCode = generateInviteCode()
        ))
        spaceMemberRepository.save(SpaceMemberEntity(
            spaceId = space.id, userId = userId, role = "OWNER"
        ))
        return SpaceResponse(
            id = space.id, name = space.name, inviteCode = space.inviteCode,
            role = "OWNER", memberCount = 1, createdAt = space.createdAt
        )
    }

    fun getSpace(userId: UUID, spaceId: UUID): SpaceResponse {
        requireMembership(userId, spaceId)
        val space = spaceRepository.findById(spaceId).orElseThrow { notFound("Space") }
        val membership = spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)!!
        val memberCount = spaceMemberRepository.findBySpaceId(spaceId).size
        return SpaceResponse(
            id = space.id, name = space.name, inviteCode = space.inviteCode,
            role = membership.role, memberCount = memberCount, createdAt = space.createdAt
        )
    }

    @Transactional
    fun updateSpace(userId: UUID, spaceId: UUID, req: UpdateSpaceRequest): SpaceResponse {
        val space = spaceRepository.findById(spaceId).orElseThrow { notFound("Space") }
        requireOwner(userId, space)
        space.name = req.name
        space.updatedAt = OffsetDateTime.now()
        spaceRepository.save(space)
        val memberCount = spaceMemberRepository.findBySpaceId(spaceId).size
        return SpaceResponse(
            id = space.id, name = space.name, inviteCode = space.inviteCode,
            role = "OWNER", memberCount = memberCount, createdAt = space.createdAt
        )
    }

    @Transactional
    fun deleteSpace(userId: UUID, spaceId: UUID) {
        val space = spaceRepository.findById(spaceId).orElseThrow { notFound("Space") }
        requireOwner(userId, space)
        spaceRepository.delete(space) // CASCADE will delete members, rooms, etc.
    }

    @Transactional
    fun refreshInviteCode(userId: UUID, spaceId: UUID): String {
        val space = spaceRepository.findById(spaceId).orElseThrow { notFound("Space") }
        requireOwner(userId, space)
        space.inviteCode = generateInviteCode()
        space.updatedAt = OffsetDateTime.now()
        spaceRepository.save(space)
        return space.inviteCode!!
    }

    @Transactional
    fun joinSpace(userId: UUID, req: JoinSpaceRequest) {
        val space = spaceRepository.findByInviteCode(req.inviteCode)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 초대 코드입니다.")
        if (spaceMemberRepository.existsBySpaceIdAndUserId(space.id, userId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 참여 중인 스페이스입니다.")
        }
        spaceJoinRequestRepository.save(SpaceJoinRequestEntity(
            spaceId = space.id, userId = userId, inviteCodeUsed = req.inviteCode
        ))
    }

    fun getMembers(userId: UUID, spaceId: UUID): List<SpaceMemberResponse> {
        requireMembership(userId, spaceId)
        val members = spaceMemberRepository.findBySpaceId(spaceId)
        return members.map { m ->
            val profile = profileRepository.findById(m.userId).orElse(null)
            SpaceMemberResponse(
                id = m.id, userId = m.userId,
                name = profile?.name, avatarUrl = profile?.avatarUrl,
                role = m.role, joinedAt = m.joinedAt
            )
        }
    }

    @Transactional
    fun removeMember(userId: UUID, spaceId: UUID, targetUserId: UUID) {
        val space = spaceRepository.findById(spaceId).orElseThrow { notFound("Space") }
        requireOwner(userId, space)
        if (targetUserId == userId) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신은 제거할 수 없습니다.")
        spaceMemberRepository.deleteBySpaceIdAndUserId(spaceId, targetUserId)
    }

    @Transactional
    fun delegateOwner(userId: UUID, spaceId: UUID, targetUserId: UUID) {
        val space = spaceRepository.findById(spaceId).orElseThrow { notFound("Space") }
        requireOwner(userId, space)
        val currentOwner = spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)!!
        val newOwner = spaceMemberRepository.findBySpaceIdAndUserId(spaceId, targetUserId)
            ?: throw notFound("Member")
        currentOwner.role = "MEMBER"
        newOwner.role = "OWNER"
        space.ownerId = targetUserId
        space.updatedAt = OffsetDateTime.now()
        spaceMemberRepository.save(currentOwner)
        spaceMemberRepository.save(newOwner)
        spaceRepository.save(space)
    }

    fun getJoinRequests(userId: UUID, spaceId: UUID): List<JoinRequestResponse> {
        val space = spaceRepository.findById(spaceId).orElseThrow { notFound("Space") }
        requireOwner(userId, space)
        val requests = spaceJoinRequestRepository.findBySpaceIdAndStatus(spaceId, "PENDING")
        return requests.map { r ->
            val profile = profileRepository.findById(r.userId).orElse(null)
            JoinRequestResponse(
                id = r.id, userId = r.userId,
                userName = profile?.name, avatarUrl = profile?.avatarUrl,
                inviteCodeUsed = r.inviteCodeUsed, status = r.status, createdAt = r.createdAt
            )
        }
    }

    @Transactional
    fun processJoinRequest(userId: UUID, spaceId: UUID, requestId: UUID, body: ProcessJoinRequestBody) {
        val space = spaceRepository.findById(spaceId).orElseThrow { notFound("Space") }
        requireOwner(userId, space)
        val req = spaceJoinRequestRepository.findById(requestId).orElseThrow { notFound("JoinRequest") }
        if (body.approve) {
            req.status = "APPROVED"
            spaceMemberRepository.save(SpaceMemberEntity(
                spaceId = spaceId, userId = req.userId, role = "MEMBER"
            ))
        } else {
            req.status = "REJECTED"
        }
        spaceJoinRequestRepository.save(req)
    }

    // ─── Helpers ──────────────────────────────────

    private fun requireMembership(userId: UUID, spaceId: UUID) {
        if (!spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "스페이스 멤버가 아닙니다.")
    }

    private fun requireOwner(userId: UUID, space: SpaceEntity) {
        if (space.ownerId != userId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "스페이스 소유자만 가능합니다.")
    }

    private fun notFound(entity: String) = ResponseStatusException(HttpStatus.NOT_FOUND, "$entity 를 찾을 수 없습니다.")

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
}
