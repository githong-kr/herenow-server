package com.nsnm.herenow.api.sample.service

import com.nsnm.herenow.api.sample.model.SampleRequest
import com.nsnm.herenow.api.sample.model.SampleResponse
import com.nsnm.herenow.fwk.core.error.BizException
import org.springframework.stereotype.Service

@Service
class SampleService {
    
    fun getSampleInfo(id: Long): SampleResponse {
        // 비즈니스 예외 테스트를 위해 id가 0이면 에러 발생
        if (id == 0L) {
            throw BizException("HNW1001", listOf("잘못된 ID 값입니다. id=$id"))
        }
        
        return SampleResponse(
            id = id,
            name = "Test User $id",
            description = "이것은 정상 처리된 응답 데이터입니다."
        )
    }
    
    fun createSample(request: SampleRequest): SampleResponse {
        return SampleResponse(
            id = 999L,
            name = request.name,
            description = request.description
        )
    }
}
