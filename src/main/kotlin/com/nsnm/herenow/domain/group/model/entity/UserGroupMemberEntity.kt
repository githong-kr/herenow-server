package com.nsnm.herenow.domain.group.model.entity

import com.nsnm.herenow.domain.group.model.enums.GroupRole
import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 그룹 내 참여자 목록 및 권한 정보
 * 기존 Supabase 의 'user_group_members' 
 */
@Entity
@Table(name = "user_group_members")
class UserGroupMemberEntity(
    @Id
    var groupMemberId: String = UUID.randomUUID().toString(),
    
    var groupId: String,
    
    var profileId: String,
    
    @Enumerated(EnumType.STRING)
    var role: GroupRole
) : BaseEntity()
