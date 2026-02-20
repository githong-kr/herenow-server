package com.nsnm.herenow.domain.user.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * 사용자 정보 엔티티
 * 기존 Supabase 의 'profiles' 테이블과 매핑됨
 */
@Entity
@Table(name = "profiles")
class ProfileEntity(
    @Id
    var profileId: String, // Supabase user.id (UUID) 를 그대로 사용
    
    var name: String,
    
    var marketingConsent: Boolean = false,
    
    var representativeGroupId: String? = null,
    
    var avatarUrl: String? = null
) : BaseEntity()
