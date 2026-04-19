package com.nsnm.herenow.api.room.controller

import com.nsnm.herenow.api.room.dto.*
import com.nsnm.herenow.api.room.service.RoomService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class RoomController(
    private val roomService: RoomService
) {

    @GetMapping("/spaces/{spaceId}/rooms")
    fun getRooms(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<List<RoomResponse>> {
        return ResponseEntity.ok(roomService.getRooms(UUID.fromString(principal.name), spaceId))
    }

    @PostMapping("/spaces/{spaceId}/rooms")
    fun createRoom(
        @PathVariable spaceId: UUID,
        @RequestBody req: CreateRoomRequest,
        principal: Principal
    ): ResponseEntity<RoomResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(roomService.createRoom(UUID.fromString(principal.name), spaceId, req))
    }

    @PutMapping("/rooms/{roomId}")
    fun updateRoom(
        @PathVariable roomId: UUID,
        @RequestBody req: UpdateRoomRequest,
        principal: Principal
    ): ResponseEntity<RoomResponse> {
        return ResponseEntity.ok(roomService.updateRoom(UUID.fromString(principal.name), roomId, req))
    }

    @DeleteMapping("/rooms/{roomId}")
    fun deleteRoom(@PathVariable roomId: UUID, principal: Principal): ResponseEntity<Void> {
        roomService.deleteRoom(UUID.fromString(principal.name), roomId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/spaces/{spaceId}/rooms/reorder")
    fun reorderRooms(
        @PathVariable spaceId: UUID,
        @RequestBody req: ReorderRoomsRequest,
        principal: Principal
    ): ResponseEntity<Void> {
        roomService.reorderRooms(UUID.fromString(principal.name), spaceId, req)
        return ResponseEntity.ok().build()
    }
}
