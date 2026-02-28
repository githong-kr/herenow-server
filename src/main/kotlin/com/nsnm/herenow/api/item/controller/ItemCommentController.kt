package com.nsnm.herenow.api.item.controller

import com.nsnm.herenow.api.item.dto.ItemCommentCreateRequest
import com.nsnm.herenow.api.item.dto.ItemCommentResponse
import com.nsnm.herenow.api.item.service.ItemCommentService
import com.nsnm.herenow.fwk.core.base.BaseController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@Tag(name = "Item Comments", description = "물건 댓글(방명록) 관리 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/items/{itemId}/comments")
class ItemCommentController(
    private val itemCommentService: ItemCommentService
) : BaseController() {

    @Operation(summary = "댓글 목록 조회", description = "특정 물건의 댓글 메세지 목록을 페이징 조회합니다.")
    @GetMapping
    fun getComments(
        @PathVariable groupId: String,
        @PathVariable itemId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(defaultValue = "frstRegTmst,desc") sort: String
    ): Page<ItemCommentResponse> {
        val sortParams = sort.split(",")
        val sortDirection = if (sortParams.getOrNull(1)?.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val sortBy = sortParams[0]
        
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
        return itemCommentService.getComments(groupId, itemId, pageable)
    }

    @Operation(summary = "댓글 작성", description = "특정 물건에 새 댓글을 남깁니다.")
    @PostMapping
    fun createComment(
        @PathVariable groupId: String,
        @PathVariable itemId: String,
        @RequestBody request: ItemCommentCreateRequest
    ): ItemCommentResponse {
        val userId = SecurityContextHolder.getContext().authentication.name
        return itemCommentService.createComment(groupId, itemId, userId, request)
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 특정 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable groupId: String,
        @PathVariable itemId: String,
        @PathVariable commentId: String
    ) {
        val userId = SecurityContextHolder.getContext().authentication.name
        itemCommentService.deleteComment(groupId, itemId, commentId, userId)
    }
}
