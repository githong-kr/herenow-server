package com.nsnm.herenow.domain.group.model.entity

import com.nsnm.herenow.domain.group.model.enums.JoinRequestStatus
import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 초대 코드를 입력하여 특정 그룹에 참가를 신청한 사용자의 대기열(Request) 정보
 */
@Entity
@Table(name = "group_join_requests")
class GroupJoinRequestEntity(
    @Id
    var requestId: String = UUID.randomUUID().toString(),
    
    var groupId: String,
    
    var profileId: String,
    
    var inviteCodeUsed: String,
    
    @Enumerated(EnumType.STRING)
    var status: JoinRequestStatus = JoinRequestStatus.PENDING
) : BaseEntity()
