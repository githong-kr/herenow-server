package com.nsnm.herenow.api.item.controller

import com.nsnm.herenow.api.item.dto.*
import com.nsnm.herenow.api.item.service.ItemService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class ItemController(
    private val itemService: ItemService
) {

    @GetMapping("/spaces/{spaceId}/items")
    fun getItems(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<List<ItemResponse>> {
        return ResponseEntity.ok(itemService.getItems(UUID.fromString(principal.name), spaceId))
    }

    @PostMapping("/spaces/{spaceId}/items")
    fun createItem(
        @PathVariable spaceId: UUID,
        @RequestBody req: CreateItemRequest,
        principal: Principal
    ): ResponseEntity<ItemResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(itemService.createItem(UUID.fromString(principal.name), spaceId, req))
    }

    @PutMapping("/items/{itemId}")
    fun updateItem(
        @PathVariable itemId: UUID,
        @RequestBody req: UpdateItemRequest,
        principal: Principal
    ): ResponseEntity<ItemResponse> {
        return ResponseEntity.ok(itemService.updateItem(UUID.fromString(principal.name), itemId, req))
    }

    @DeleteMapping("/items/{itemId}")
    fun deleteItem(@PathVariable itemId: UUID, principal: Principal): ResponseEntity<Void> {
        itemService.deleteItem(UUID.fromString(principal.name), itemId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/items/{itemId}/assign")
    fun assignItem(
        @PathVariable itemId: UUID,
        @RequestBody req: AssignItemRequest,
        principal: Principal
    ): ResponseEntity<ItemResponse> {
        return ResponseEntity.ok(itemService.assignItem(UUID.fromString(principal.name), itemId, req))
    }
}
