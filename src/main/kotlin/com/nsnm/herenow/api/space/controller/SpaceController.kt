package com.nsnm.herenow.api.space.controller

import com.nsnm.herenow.api.space.dto.*
import com.nsnm.herenow.api.space.service.SpaceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class SpaceController(
    private val spaceService: SpaceService
) {

    @GetMapping("/spaces")
    fun getMySpaces(principal: Principal): ResponseEntity<List<SpaceResponse>> {
        return ResponseEntity.ok(spaceService.getMySpaces(UUID.fromString(principal.name)))
    }

    @PostMapping("/spaces")
    fun createSpace(@RequestBody req: CreateSpaceRequest, principal: Principal): ResponseEntity<SpaceResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(spaceService.createSpace(UUID.fromString(principal.name), req))
    }

    @GetMapping("/spaces/{spaceId}")
    fun getSpace(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<SpaceResponse> {
        return ResponseEntity.ok(spaceService.getSpace(UUID.fromString(principal.name), spaceId))
    }

    @PutMapping("/spaces/{spaceId}")
    fun updateSpace(
        @PathVariable spaceId: UUID,
        @RequestBody req: UpdateSpaceRequest,
        principal: Principal
    ): ResponseEntity<SpaceResponse> {
        return ResponseEntity.ok(spaceService.updateSpace(UUID.fromString(principal.name), spaceId, req))
    }

    @DeleteMapping("/spaces/{spaceId}")
    fun deleteSpace(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<Void> {
        spaceService.deleteSpace(UUID.fromString(principal.name), spaceId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/spaces/{spaceId}/invite/refresh")
    fun refreshInviteCode(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<Map<String, String>> {
        val code = spaceService.refreshInviteCode(UUID.fromString(principal.name), spaceId)
        return ResponseEntity.ok(mapOf("inviteCode" to code))
    }

    @PostMapping("/spaces/join")
    fun joinSpace(@RequestBody req: JoinSpaceRequest, principal: Principal): ResponseEntity<Void> {
        spaceService.joinSpace(UUID.fromString(principal.name), req)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/spaces/{spaceId}/members")
    fun getMembers(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<List<SpaceMemberResponse>> {
        return ResponseEntity.ok(spaceService.getMembers(UUID.fromString(principal.name), spaceId))
    }

    @DeleteMapping("/spaces/{spaceId}/members/{userId}")
    fun removeMember(
        @PathVariable spaceId: UUID,
        @PathVariable userId: UUID,
        principal: Principal
    ): ResponseEntity<Void> {
        spaceService.removeMember(UUID.fromString(principal.name), spaceId, userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/spaces/{spaceId}/members/{userId}/delegate")
    fun delegateOwner(
        @PathVariable spaceId: UUID,
        @PathVariable userId: UUID,
        principal: Principal
    ): ResponseEntity<Void> {
        spaceService.delegateOwner(UUID.fromString(principal.name), spaceId, userId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/spaces/{spaceId}/join-requests")
    fun getJoinRequests(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<List<JoinRequestResponse>> {
        return ResponseEntity.ok(spaceService.getJoinRequests(UUID.fromString(principal.name), spaceId))
    }

    @PostMapping("/spaces/{spaceId}/join-requests/{requestId}/process")
    fun processJoinRequest(
        @PathVariable spaceId: UUID,
        @PathVariable requestId: UUID,
        @RequestBody body: ProcessJoinRequestBody,
        principal: Principal
    ): ResponseEntity<Void> {
        spaceService.processJoinRequest(UUID.fromString(principal.name), spaceId, requestId, body)
        return ResponseEntity.ok().build()
    }
}
