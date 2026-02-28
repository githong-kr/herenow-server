package com.nsnm.herenow.api.user.v1

import com.nsnm.herenow.api.user.service.UserService
import com.nsnm.herenow.api.user.v1.dto.MyGroupDto
import com.nsnm.herenow.api.user.v1.dto.UpdateProfileRequest
import com.nsnm.herenow.api.user.v1.dto.UserRegistrationRequest
import com.nsnm.herenow.api.user.v1.dto.UserRegistrationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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

    @Operation(summary = "내 그룹 목록 조회", description = "로그인한 사용자가 속한 모든 스페이스(그룹)의 기본 정보 및 권한을 리스트로 반환합니다.")
    @GetMapping("/me/groups")
    fun getMyGroups(): List<MyGroupDto> {
        val uid = SecurityContextHolder.getContext().authentication.name
        return userService.getMyGroups(uid)
    }

    @Operation(summary = "기본 스페이스(대표 그룹) 변경", description = "사용자가 속한 그룹 중 하나를 현재 활성화된 메인 스페이스로 설정합니다.")
    @PutMapping("/me/default-group/{groupId}")
    fun setDefaultGroup(@PathVariable groupId: String) {
        val uid = SecurityContextHolder.getContext().authentication.name
        userService.setDefaultGroup(uid, groupId)
    }

    @Operation(summary = "내 프로필 수정", description = "로그인한 사용자의 닉네임과 프로필 사진을 변경합니다.")
    @PutMapping("/me/profile")
    fun updateProfile(
        @RequestBody request: UpdateProfileRequest
    ): UserRegistrationResponse {
        val uid = SecurityContextHolder.getContext().authentication.name
        return userService.updateProfile(uid, request)
    }

    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 모든 정보(프로필, 구성원, 빈 그룹)를 파기합니다. 방장인 스페이스에 다른 멤버가 있다면 예외를 반환합니다.")
    @DeleteMapping("/me")
    fun withdrawAccount() {
        val uid = SecurityContextHolder.getContext().authentication.name
        userService.withdrawAccount(uid)
    }
}
