package com.nsnm.herenow.domain.space.repository

import com.nsnm.herenow.domain.space.entity.SpaceMemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpaceMemberRepository : JpaRepository<SpaceMemberEntity, UUID> {
    fun findByUserId(userId: UUID): List<SpaceMemberEntity>
    fun findBySpaceId(spaceId: UUID): List<SpaceMemberEntity>
    fun findBySpaceIdAndUserId(spaceId: UUID, userId: UUID): SpaceMemberEntity?
    fun existsBySpaceIdAndUserId(spaceId: UUID, userId: UUID): Boolean
    fun deleteBySpaceIdAndUserId(spaceId: UUID, userId: UUID)
}
