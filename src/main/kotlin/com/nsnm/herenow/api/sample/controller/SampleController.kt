package com.nsnm.herenow.api.sample.controller

import com.nsnm.herenow.api.sample.model.SampleRequest
import com.nsnm.herenow.api.sample.model.SampleResponse
import com.nsnm.herenow.api.sample.service.SampleService
import com.nsnm.herenow.fwk.core.base.BaseController
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sample")
class SampleController(
    private val sampleService: SampleService
) : BaseController() {

    @GetMapping("/{id}")
    @Operation(summary = "샘플 조회", description = "특정 샘플을 조회합니다.")
    fun getSample(@PathVariable id: Long): SampleResponse {
        return sampleService.getSampleInfo(id)
    }

    @PostMapping
    @Operation(summary = "샘플 생성", description = "샘플을 생성합니다.")
    fun createSample(@RequestBody request: SampleRequest): SampleResponse {
        return sampleService.createSample(request)
    }
}
