package com.nsnm.herenow.api.item.service

import com.nsnm.herenow.api.item.dto.ItemCommentCreateRequest
import com.nsnm.herenow.api.item.dto.ItemCommentResponse
import com.nsnm.herenow.domain.item.model.entity.ItemCommentEntity
import com.nsnm.herenow.domain.item.repository.ItemCommentRepository
import com.nsnm.herenow.domain.item.repository.ItemRepository
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import com.nsnm.herenow.fwk.core.base.BaseService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemCommentService(
    private val itemCommentRepository: ItemCommentRepository,
    private val itemRepository: ItemRepository,
    private val profileRepository: ProfileRepository
) : BaseService() {

    @Transactional(readOnly = true)
    fun getComments(groupId: String, itemId: String, pageable: Pageable): Page<ItemCommentResponse> {
        val commentsPage = itemCommentRepository.findByGroupIdAndItemId(groupId, itemId, pageable)

        return commentsPage.map { comment ->
            val writerProfile = profileRepository.findById(comment.writerId).orElse(null)
            ItemCommentResponse(
                commentId = comment.commentId,
                itemId = comment.itemId,
                groupId = comment.groupId,
                writerId = comment.writerId,
                writerName = writerProfile?.name ?: "알 수 없음",
                writerAvatarUrl = writerProfile?.avatarUrl,
                content = comment.content,
                createdAt = comment.frstRegTmst?.toString() ?: ""
            )
        }
    }

    @Transactional
    fun createComment(
        groupId: String,
        itemId: String,
        writerId: String,
        request: ItemCommentCreateRequest
    ): ItemCommentResponse {
        val item = itemRepository.findById(itemId).orElse(null)
        require(item != null && item.groupId == groupId) { "해당 물건을 찾을 수 없습니다." }

        var comment = ItemCommentEntity(
            itemId = itemId,
            groupId = groupId,
            writerId = writerId,
            content = request.content
        )
        
        comment = itemCommentRepository.save(comment)
        val writerProfile = profileRepository.findById(writerId).orElse(null)

        return ItemCommentResponse(
            commentId = comment.commentId,
            itemId = comment.itemId,
            groupId = comment.groupId,
            writerId = comment.writerId,
            writerName = writerProfile?.name ?: "알 수 없음",
            writerAvatarUrl = writerProfile?.avatarUrl,
            content = comment.content,
            createdAt = comment.frstRegTmst?.toString() ?: "" // Transaction 묶여있어 실제 flush 전엔 널일 수 있지만 리턴용
        )
    }

    @Transactional
    fun deleteComment(groupId: String, itemId: String, commentId: String, writerId: String) {
        val comment = itemCommentRepository.findByCommentIdAndWriterId(commentId, writerId)
            ?: throw IllegalArgumentException("존재하지 않는 댓글이거나 권한이 없습니다.")
            
        require(comment.groupId == groupId && comment.itemId == itemId) { "잘못된 경로 접근입니다." }
        
        itemCommentRepository.deleteByGroupIdAndItemIdAndCommentId(groupId, itemId, commentId)
    }
}
