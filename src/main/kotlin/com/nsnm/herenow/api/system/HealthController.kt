package com.nsnm.herenow.api.system

import com.nsnm.herenow.fwk.core.base.BaseController
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
class HealthController : BaseController() {

    @GetMapping("/")
    fun root(): String {
        return "HereNow Server is UP"
    }

    @GetMapping("/actuator/health")
    fun health(): String {
        return "UP"
    }
}
