package com.nsnm.herenow.api.user.v1

import com.nsnm.herenow.api.user.service.UserGroupService
import com.nsnm.herenow.api.user.v1.dto.GroupJoinRequestDto
import com.nsnm.herenow.api.user.v1.dto.GroupMemberDto
import com.nsnm.herenow.api.user.v1.dto.JoinGroupRequest
import com.nsnm.herenow.api.user.v1.dto.ProcessJoinRequest
import com.nsnm.herenow.api.user.v1.dto.UserGroupDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.nsnm.herenow.fwk.core.base.BaseController

@Tag(name = "User Groups", description = "사용자 그룹 및 권한 관리 API")
@RestController
@RequestMapping("/api/v1/groups")
class UserGroupController(
    private val userGroupService: UserGroupService
) : BaseController() {

    @Operation(summary = "해당 그룹 기본 정보 조회", description = "그룹의 소유주와 현재 설정된 초대 코드 정보를 조회합니다.")
    @GetMapping("/{groupId}")
    fun getGroupInfo(@PathVariable groupId: String): UserGroupDto {
        return userGroupService.getGroupInfo(groupId)
    }

    @Operation(summary = "그룹 멤버 리스트 조회", description = "해당 그룹에 속한 사용자 프로필 ID 및 권한 목록을 조회합니다.")
    @GetMapping("/{groupId}/members")
    fun getGroupMembers(@PathVariable groupId: String): List<GroupMemberDto> {
        return userGroupService.getGroupMembers(groupId)
    }

    @Operation(summary = "초대 코드 갱신", description = "그룹의 소유자(OWNER)가 8자리 단축 초대 코드를 새로 갱신(발급)합니다.")
    @PostMapping("/{groupId}/invite-code")
    fun refreshInviteCode(@PathVariable groupId: String): String {
        val uid = SecurityContextHolder.getContext().authentication.name
        return userGroupService.refreshInviteCode(groupId, uid)
    }

    @Operation(summary = "가입 대기 요청 목록 조회", description = "그룹의 소유자(OWNER)가 PENDING 상태인 가입 요청(대기열)들을 리스트업합니다.")
    @GetMapping("/{groupId}/join-requests")
    fun getPendingJoinRequests(@PathVariable groupId: String): List<GroupJoinRequestDto> {
        val uid = SecurityContextHolder.getContext().authentication.name
        return userGroupService.getPendingJoinRequests(groupId, uid)
    }

    @Operation(summary = "초대 코드로 그룹 가입 요청", description = "전달받은 초대 코드를 입력하여 해당 그룹에 합류하기 위한 승인 대기열(PENDING)에 등록합니다.")
    @PostMapping("/join")
    fun joinGroupByInviteCode(@RequestBody request: JoinGroupRequest): GroupJoinRequestDto {
        val uid = SecurityContextHolder.getContext().authentication.name
        return userGroupService.joinGroupByInviteCode(request.inviteCode, uid)
    }

    @Operation(summary = "대기열 요청 승인/거절 처리", description = "그룹의 소유자(OWNER)가 대기 목록 내 특정 요청을 최종 승인(APPROVED) 혹은 거절(REJECTED) 처리합니다. 승인 시 그룹 멤버로 등록됩니다.")
    @PostMapping("/{groupId}/join-requests/process")
    fun processJoinRequest(
        @PathVariable groupId: String,
        @RequestBody request: ProcessJoinRequest
    ) {
        val uid = SecurityContextHolder.getContext().authentication.name
        userGroupService.processJoinRequest(groupId, request.requestId, uid, request.approve)
    }
}
