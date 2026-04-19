package com.nsnm.herenow.api.home.controller

import com.nsnm.herenow.api.home.dto.HomeResponse
import com.nsnm.herenow.api.home.service.HomeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class HomeController(
    private val homeService: HomeService
) {

    @GetMapping("/spaces/{spaceId}/home")
    fun getHomeData(@PathVariable spaceId: UUID, principal: Principal): ResponseEntity<HomeResponse> {
        return ResponseEntity.ok(homeService.getHomeData(UUID.fromString(principal.name), spaceId))
    }
}
