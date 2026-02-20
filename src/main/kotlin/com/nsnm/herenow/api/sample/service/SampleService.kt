package com.nsnm.herenow.api.sample.service

import com.nsnm.herenow.api.sample.model.SampleEntity
import com.nsnm.herenow.api.sample.model.SampleRequest
import com.nsnm.herenow.api.sample.model.SampleResponse
import com.nsnm.herenow.api.sample.repository.SampleRepository
import com.nsnm.herenow.fwk.core.error.BizException
import org.springframework.stereotype.Service

@Service
class SampleService(
    private val sampleRepository: SampleRepository
) {
    
    fun getSampleInfo(id: Long): SampleResponse {
        // 비즈니스 예외 테스트를 위해 id가 0이면 에러 발생
        if (id == 0L) {
            throw BizException("HNW1001", listOf("잘못된 ID 값입니다. id=$id"))
        }

        val entity = sampleRepository.findById(id)
            .orElseThrow { BizException("NEW3001", listOf("Sample Data")) }
        
        return SampleResponse(
            id = entity.id!!,
            name = entity.name,
            description = entity.description,
            frstRegTmst = entity.frstRegTmst,
            frstRegGuid = entity.frstRegGuid,
            lastChngTmst = entity.lastChngTmst,
            lastChngGuid = entity.lastChngGuid
        )
    }
    
    fun createSample(request: SampleRequest): SampleResponse {
        val entity = SampleEntity(
            name = request.name,
            description = request.description
        )
        val savedEntity = sampleRepository.save(entity)

        return SampleResponse(
            id = savedEntity.id!!,
            name = savedEntity.name,
            description = savedEntity.description,
            frstRegTmst = savedEntity.frstRegTmst,
            frstRegGuid = savedEntity.frstRegGuid,
            lastChngTmst = savedEntity.lastChngTmst,
            lastChngGuid = savedEntity.lastChngGuid
        )
    }
}
