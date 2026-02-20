package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 아이템의 보관 장소
 * 트리 구조 (parentLocationId) 를 가질 수 있음
 */
@Entity
@Table(name = "locations")
class LocationEntity(
    @Id
    var locationId: String = UUID.randomUUID().toString(),
    
    var groupId: String,
    
    var locationName: String,
    
    var iconName: String? = null,
    
    var parentLocationId: String? = null,
    
    var displayOrder: Int = 0
) : BaseEntity()
