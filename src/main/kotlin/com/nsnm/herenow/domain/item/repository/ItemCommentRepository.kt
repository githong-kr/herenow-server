package com.nsnm.herenow.domain.item.repository

import com.nsnm.herenow.domain.item.model.entity.ItemCommentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemCommentRepository : JpaRepository<ItemCommentEntity, String> {
    fun findByGroupIdAndItemId(groupId: String, itemId: String, pageable: Pageable): Page<ItemCommentEntity>
    fun deleteByGroupIdAndItemIdAndCommentId(groupId: String, itemId: String, commentId: String)
    fun findByCommentIdAndWriterId(commentId: String, writerId: String): ItemCommentEntity?
    fun deleteAllByItemId(itemId: String)
}
