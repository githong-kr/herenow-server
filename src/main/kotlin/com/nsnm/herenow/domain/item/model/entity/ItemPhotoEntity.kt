package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

import jakarta.persistence.EntityListeners

/**
 * 아이템 상세 사진 정보
 */
@Entity
@Table(name = "item_photos")
class ItemPhotoEntity(
    @Id
    var photoId: String = UUID.randomUUID().toString(),
    
    var itemId: String,
    
    var photoUrl: String,
    
    var fileName: String? = null,
    
    var displayOrder: Int = 0,
    
    var fileSize: Long? = null,
    
    var mimeType: String? = null
) : BaseEntity()
