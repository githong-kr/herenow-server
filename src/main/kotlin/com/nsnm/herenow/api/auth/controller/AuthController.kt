package com.nsnm.herenow.api.auth.controller

import com.nsnm.herenow.api.auth.dto.InitRequest
import com.nsnm.herenow.api.auth.dto.InitResponse
import com.nsnm.herenow.api.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/init")
    fun init(
        @RequestBody req: InitRequest,
        principal: Principal
    ): ResponseEntity<InitResponse> {
        val userId = UUID.fromString(principal.name)
        return ResponseEntity.ok(authService.initUser(userId, req))
    }
}
