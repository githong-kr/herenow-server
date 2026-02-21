package com.nsnm.herenow.api.home.v1

import com.nsnm.herenow.api.home.service.HomeService
import com.nsnm.herenow.api.home.v1.dto.HomeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Home", description = "홈 화면 대시보드 API")
@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val homeService: HomeService
) {

    @Operation(summary = "홈 화면 데이터 조회", description = "로그인한 사용자의 대표 그룹(Workspace)을 기준으로 홈 화면 위젯을 위한 통계 및 리스트를 조회합니다.")
    @GetMapping
    fun getHomeData(): HomeResponse {
        val authentication = SecurityContextHolder.getContext().authentication
        val uid = authentication.name

        return homeService.getHomeDashboardData(uid)
    }
}
