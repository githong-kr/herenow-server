package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

import jakarta.persistence.EntityListeners

/**
 * 아이템 다중 태그 매핑
 */
@Entity
@Table(name = "item_tags")
class ItemTagEntity(
    @Id
    var itemTagId: String = UUID.randomUUID().toString(),
    
    var itemId: String,
    
    var tagId: String
) : BaseEntity()
