package com.nsnm.herenow.domain.group.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 사용자가 소속된 메인 그룹(공간) 정보
 * 기존 Supabase 의 'user_groups' 테이블과 매핑
 */
@Entity
@Table(name = "user_groups")
class UserGroupEntity(
    @Id
    var groupId: String = UUID.randomUUID().toString(),
    
    var groupName: String,
    
    var ownerProfileId: String,
    
    var inviteCode: String? = null
) : BaseEntity()
