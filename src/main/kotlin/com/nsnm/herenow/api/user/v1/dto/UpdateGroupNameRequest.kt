package com.nsnm.herenow.api.user.v1.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스페이스명(그룹명) 수정 요청")
data class UpdateGroupNameRequest(
    @Schema(description = "새로운 스페이스 이름", example = "새로운 우리집")
    val groupName: String
)
