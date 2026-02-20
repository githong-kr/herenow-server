package com.nsnm.herenow.api.item.v1

import com.nsnm.herenow.api.item.service.ItemService
import com.nsnm.herenow.api.item.v1.dto.CreateItemRequest
import com.nsnm.herenow.api.item.v1.dto.ItemResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Items", description = "실제 아이템 재고 증감 및 관리 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/items")
class ItemController(
    private val itemService: ItemService
) {

    @Operation(summary = "아이템 생성", description = "아이템 기본 정보, 사진, 태그를 한 번에 등록합니다.")
    @PostMapping
    fun createItem(
        @PathVariable groupId: String,
        @RequestBody request: CreateItemRequest
    ): ItemResponse {
        return itemService.createItem(groupId, request)
    }
}
