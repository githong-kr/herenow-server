package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 아이템 메타 태그
 */
@Entity
@Table(name = "tags")
class TagEntity(
    @Id
    var tagId: String = UUID.randomUUID().toString(),
    
    var groupId: String,
    
    var tagName: String
) : BaseEntity()
