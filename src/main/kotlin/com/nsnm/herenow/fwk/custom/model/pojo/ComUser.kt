package com.nsnm.herenow.fwk.custom.model.pojo

data class ComUser(
    var userId: String = "",              // 사용자 ID
    var userNm: String = "",              // 사용자명
    var userRole: String = "",            // 사용자 역할
    var authToken: String = "",           // 인증 토큰
    var isAuthenticated: Boolean = false  // 인증 여부
)