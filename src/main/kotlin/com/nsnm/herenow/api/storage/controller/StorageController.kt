package com.nsnm.herenow.api.storage.controller

import com.nsnm.herenow.api.storage.dto.*
import com.nsnm.herenow.api.storage.service.StorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class StorageController(
    private val storageService: StorageService
) {

    @GetMapping("/rooms/{roomId}/storages")
    fun getStorages(@PathVariable roomId: UUID, principal: Principal): ResponseEntity<List<StorageResponse>> {
        return ResponseEntity.ok(storageService.getStorages(UUID.fromString(principal.name), roomId))
    }

    @PostMapping("/rooms/{roomId}/storages")
    fun createStorage(
        @PathVariable roomId: UUID,
        @RequestBody req: CreateStorageRequest,
        principal: Principal
    ): ResponseEntity<StorageResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(storageService.createStorage(UUID.fromString(principal.name), roomId, req))
    }

    @PutMapping("/storages/{storageId}")
    fun updateStorage(
        @PathVariable storageId: UUID,
        @RequestBody req: UpdateStorageRequest,
        principal: Principal
    ): ResponseEntity<StorageResponse> {
        return ResponseEntity.ok(storageService.updateStorage(UUID.fromString(principal.name), storageId, req))
    }

    @DeleteMapping("/storages/{storageId}")
    fun deleteStorage(@PathVariable storageId: UUID, principal: Principal): ResponseEntity<Void> {
        storageService.deleteStorage(UUID.fromString(principal.name), storageId)
        return ResponseEntity.noContent().build()
    }
}
