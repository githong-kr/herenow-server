package com.nsnm.herenow.api.profile.controller

import com.nsnm.herenow.api.profile.dto.ProfileResponse
import com.nsnm.herenow.api.profile.dto.UpdateProfileRequest
import com.nsnm.herenow.api.profile.service.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/profile")
class ProfileController(
    private val profileService: ProfileService
) {

    @GetMapping
    fun getProfile(principal: Principal): ResponseEntity<ProfileResponse> {
        return ResponseEntity.ok(profileService.getProfile(UUID.fromString(principal.name)))
    }

    @PutMapping
    fun updateProfile(
        @RequestBody req: UpdateProfileRequest,
        principal: Principal
    ): ResponseEntity<ProfileResponse> {
        return ResponseEntity.ok(profileService.updateProfile(UUID.fromString(principal.name), req))
    }
}
