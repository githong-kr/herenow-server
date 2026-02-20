package com.nsnm.herenow.domain.group.repository

import com.nsnm.herenow.domain.group.model.entity.GroupJoinRequestEntity
import com.nsnm.herenow.domain.group.model.enums.JoinRequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GroupJoinRequestRepository : JpaRepository<GroupJoinRequestEntity, String> {
    fun findByGroupIdAndStatus(groupId: String, status: JoinRequestStatus): List<GroupJoinRequestEntity>
    fun findByGroupIdAndProfileIdAndStatus(groupId: String, profileId: String, status: JoinRequestStatus): GroupJoinRequestEntity?
}
