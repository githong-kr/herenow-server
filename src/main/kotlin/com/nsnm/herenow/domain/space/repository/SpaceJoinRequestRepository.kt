package com.nsnm.herenow.domain.space.repository

import com.nsnm.herenow.domain.space.entity.SpaceJoinRequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpaceJoinRequestRepository : JpaRepository<SpaceJoinRequestEntity, UUID> {
    fun findBySpaceIdAndStatus(spaceId: UUID, status: String): List<SpaceJoinRequestEntity>
    fun findByUserIdAndSpaceId(userId: UUID, spaceId: UUID): SpaceJoinRequestEntity?
}
