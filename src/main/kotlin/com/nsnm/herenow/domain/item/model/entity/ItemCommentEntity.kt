package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 아이템 단말로 작성되는 방명록/댓글 정보
 */
@Entity
@Table(name = "item_comments")
class ItemCommentEntity(
    @Id
    var commentId: String = UUID.randomUUID().toString(),
    
    var itemId: String,
    
    var groupId: String,
    
    var writerId: String, // profileId
    
    @Column(columnDefinition = "TEXT")
    var content: String
) : BaseEntity()
