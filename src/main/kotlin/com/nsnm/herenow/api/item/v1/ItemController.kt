package com.nsnm.herenow.api.item.v1

import com.nsnm.herenow.api.item.service.ItemService
import com.nsnm.herenow.api.item.v1.dto.CreateItemRequest
import com.nsnm.herenow.api.item.v1.dto.ItemResponse
import com.nsnm.herenow.api.item.v1.dto.UpdateItemRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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

    @Operation(summary = "아이템 생성", description = "아이템 기본 정보, 사진, 태그를 한 번에 등록합니다.")
    @PostMapping
    fun createItem(
        @PathVariable groupId: String,
        @RequestBody request: CreateItemRequest
    ): ItemResponse {
        return itemService.createItem(groupId, request)
    }

    @Operation(summary = "아이템 수정", description = "아이템 정보 및 관련 메타데이터를 갱신합니다.")
    @PutMapping("/{itemId}")
    fun updateItem(
        @PathVariable groupId: String,
        @PathVariable itemId: String,
        @RequestBody request: UpdateItemRequest
    ): ItemResponse {
        return itemService.updateItem(groupId, itemId, request)
    }

    @Operation(summary = "아이템 삭제", description = "아이템과 연관된 태그, 사진 정보를 함께 삭제합니다.")
    @DeleteMapping("/{itemId}")
    fun deleteItem(
        @PathVariable groupId: String,
        @PathVariable itemId: String
    ) {
        itemService.deleteItem(groupId, itemId)
    }
}
