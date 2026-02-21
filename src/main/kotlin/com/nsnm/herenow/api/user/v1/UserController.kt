package com.nsnm.herenow.api.user.v1

import com.nsnm.herenow.api.user.service.UserService
import com.nsnm.herenow.api.user.v1.dto.UserRegistrationRequest
import com.nsnm.herenow.api.user.v1.dto.UserRegistrationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.nsnm.herenow.fwk.core.base.BaseController

@Tag(name = "Users", description = "사용자 프로필 관리 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) : BaseController() {

    @Operation(summary = "사용자 가입 및 초기화", description = "JWT 토큰의 UID를 바탕으로 프로필과 기본 그룹을 생성합니다.")
    @PostMapping("/init")
    fun initializeUser(
        @RequestBody request: UserRegistrationRequest
    ): UserRegistrationResponse {
        val authentication = SecurityContextHolder.getContext().authentication
        val uid = authentication.name // JwtAuthenticationFilter에서 name 에 해당 uid를 박아 넣었음

        return userService.registerInitialUser(uid, request)
    }
}
