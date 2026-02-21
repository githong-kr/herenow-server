package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 아이템의 품목 카테고리
 * 계층 구조 대신 단순 UI 그룹핑을 위한 (categoryGroup) 필드를 가짐
 */
@Entity
@Table(name = "categories")
class CategoryEntity(
    @Id
    var categoryId: String = UUID.randomUUID().toString(),
    
    var groupId: String,
    
    var categoryName: String,
    
    var iconName: String? = null,
    
    var categoryGroup: String? = null,
    
    var displayOrder: Int = 0
) : BaseEntity()
