package com.nsnm.herenow.domain.space.repository

import com.nsnm.herenow.domain.space.entity.SpaceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpaceRepository : JpaRepository<SpaceEntity, UUID> {
    fun findByOwnerId(ownerId: UUID): List<SpaceEntity>
    fun findByInviteCode(inviteCode: String): SpaceEntity?
}
