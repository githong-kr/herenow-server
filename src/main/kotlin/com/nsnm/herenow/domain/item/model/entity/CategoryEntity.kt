package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 아이템의 품목 카테고리
 * 트리 구조 (parentCategoryId) 를 가질 수 있음
 */
@Entity
@Table(name = "categories")
class CategoryEntity(
    @Id
    var categoryId: String = UUID.randomUUID().toString(),
    
    var groupId: String,
    
    var categoryName: String,
    
    var iconName: String? = null,
    
    var parentCategoryId: String? = null,
    
    var displayOrder: Int = 0
) : BaseEntity()
