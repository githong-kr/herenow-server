package com.nsnm.herenow.api.item.v1

import com.nsnm.herenow.api.item.service.ItemService
import com.nsnm.herenow.api.item.v1.dto.CreateItemRequest
import com.nsnm.herenow.api.item.v1.dto.ItemResponse
import com.nsnm.herenow.api.item.v1.dto.UpdateItemRequest
import com.nsnm.herenow.api.item.v1.dto.SearchItemRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.nsnm.herenow.fwk.core.base.BaseController

@Tag(name = "Items", description = "실제 아이템 재고 증감 및 관리 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/items")
class ItemController(
    private val itemService: ItemService
) : BaseController() {

    @Operation(summary = "아이템 필터링 및 목록 조회", description = "조건에 따라 아이템들을 페이징 단위로 검색합니다.")
    @GetMapping
    fun getItems(
        @PathVariable groupId: String,
        @ParameterObject request: SearchItemRequest,
        @PageableDefault(size = 20) @ParameterObject pageable: Pageable
    ): Page<ItemResponse> {
        return itemService.getItems(groupId, request, pageable)
    }

    @Operation(summary = "아이템 단건 조회", description = "아이템의 상세 정보를 조회합니다.")
    @GetMapping("/{itemId}")
    fun getItem(
        @PathVariable groupId: String,
        @PathVariable itemId: String
    ): ItemResponse {
        return itemService.getItem(groupId, itemId)
    }

    @Operation(summary = "아이템 생성", description = "아이템 기본 정보, 사진, 태그를 한 번에 등록합니다.")
    @PostMapping
    fun createItem(
        @PathVariable groupId: String,
        @RequestBody request: CreateItemRequest
    ): ItemResponse {
        val uid = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication.name
        return itemService.createItem(groupId, uid, request)
    }

    @Operation(summary = "아이템 수정", description = "아이템 정보 및 관련 메타데이터를 갱신합니다.")
    @PutMapping("/{itemId}")
    fun updateItem(
        @PathVariable groupId: String,
        @PathVariable itemId: String,
        @RequestBody request: UpdateItemRequest
    ): ItemResponse {
        val uid = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication.name
        return itemService.updateItem(groupId, uid, itemId, request)
    }

    @Operation(summary = "아이템 삭제", description = "아이템과 연관된 태그, 사진 정보를 함께 삭제합니다.")
    @DeleteMapping("/{itemId}")
    fun deleteItem(
        @PathVariable groupId: String,
        @PathVariable itemId: String
    ) {
        val uid = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication.name
        itemService.deleteItem(groupId, uid, itemId)
    }
}
