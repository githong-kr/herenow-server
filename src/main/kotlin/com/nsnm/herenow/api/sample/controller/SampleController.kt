package com.nsnm.herenow.api.sample.controller

import com.nsnm.herenow.api.sample.model.SampleRequest
import com.nsnm.herenow.api.sample.model.SampleResponse
import com.nsnm.herenow.api.sample.service.SampleService
import com.nsnm.herenow.fwk.core.base.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sample")
class SampleController(
    private val sampleService: SampleService
) : BaseController() {

    @GetMapping("/{id}")
    fun getSample(@PathVariable id: Long): SampleResponse {
        return sampleService.getSampleInfo(id)
    }

    @PostMapping
    fun createSample(@RequestBody request: SampleRequest): SampleResponse {
        return sampleService.createSample(request)
    }
}
