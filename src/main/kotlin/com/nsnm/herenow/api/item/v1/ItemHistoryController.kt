package com.nsnm.herenow.api.item.v1

import com.nsnm.herenow.api.item.service.ItemService
import com.nsnm.herenow.api.item.v1.dto.ItemHistoryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Item History API", description = "아이템(물건) 변경 이력 열람 목적의 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/items/{itemId}/history")
class ItemHistoryController(
    private val itemService: ItemService
) {

    @Operation(summary = "아이템 이력 조회", description = "특정 아이템의 생성, 수정, 삭제 로그 내역을 최신순으로 반환합니다.")
    @GetMapping
    fun getItemHistory(
        @PathVariable groupId: String,
        @PathVariable itemId: String
    ): List<ItemHistoryResponse> {
        return itemService.getItemHistory(itemId)
    }
}
