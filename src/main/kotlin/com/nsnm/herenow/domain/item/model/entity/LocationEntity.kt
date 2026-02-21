package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 아이템의 보관 장소
 * 계층 구조 대신 단순 UI 그룹핑을 위한 (locationGroup) 필드를 가짐
 */
@Entity
@Table(name = "locations")
class LocationEntity(
    @Id
    var locationId: String = UUID.randomUUID().toString(),
    
    var groupId: String,
    
    var locationName: String,
    
    var iconName: String? = null,
    
    var photoUrl: String? = null,
    
    var locationGroup: String? = null,
    
    var displayOrder: Int = 0
) : BaseEntity()
