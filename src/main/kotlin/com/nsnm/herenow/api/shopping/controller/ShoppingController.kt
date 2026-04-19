package com.nsnm.herenow.api.shopping.controller

import com.nsnm.herenow.api.shopping.dto.*
import com.nsnm.herenow.api.shopping.service.ShoppingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class ShoppingController(
    private val shoppingService: ShoppingService
) {

    @GetMapping("/spaces/{spaceId}/shopping-list")
    fun getShoppingList(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<List<ShoppingItemResponse>> {
        return ResponseEntity.ok(shoppingService.getShoppingList(UUID.fromString(principal.name), spaceId))
    }

    @PostMapping("/spaces/{spaceId}/shopping-list")
    fun addItem(
        @PathVariable spaceId: UUID,
        @RequestBody req: CreateShoppingItemRequest,
        principal: Principal
    ): ResponseEntity<ShoppingItemResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(shoppingService.addItem(UUID.fromString(principal.name), spaceId, req))
    }

    @PutMapping("/shopping-list/{id}/toggle")
    fun toggleItem(@PathVariable id: UUID, principal: Principal): ResponseEntity<ShoppingItemResponse> {
        return ResponseEntity.ok(shoppingService.toggleItem(UUID.fromString(principal.name), id))
    }

    @DeleteMapping("/shopping-list/{id}")
    fun deleteItem(@PathVariable id: UUID, principal: Principal): ResponseEntity<Void> {
        shoppingService.deleteItem(UUID.fromString(principal.name), id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/spaces/{spaceId}/shopping-list/purchased")
    fun clearPurchased(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<Void> {
        shoppingService.clearPurchased(UUID.fromString(principal.name), spaceId)
        return ResponseEntity.noContent().build()
    }
}
