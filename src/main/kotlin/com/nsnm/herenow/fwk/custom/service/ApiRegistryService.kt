package com.nsnm.herenow.fwk.custom.service

import com.nsnm.herenow.fwk.core.error.UnauthorizedException
import com.nsnm.herenow.lib.ext.logger
import com.nsnm.herenow.lib.model.entity.log.ApiEntity
import com.nsnm.herenow.lib.model.repository.log.ApiRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap
import com.nsnm.herenow.fwk.core.base.BaseService

@Service
class ApiRegistryService(
    private val apiRepository: ApiRepository
) : BaseService() {

    // API 정보를 메모리에 들고 있기 위한 캐시 (Key: "HTTPMETHOD-URLPATH")
    private val apiCache = ConcurrentHashMap<String, ApiEntity>()    /**
     * 서버 기동 완료 시점에 전체 원장을 메모리에 적재
     */
    @Transactional(readOnly = true)
    fun initializeCache() {
        log.info("API 원장 메모리 캐시 로딩 시작")
        val allApis = apiRepository.findAll()
        apiCache.clear()
        allApis.forEach { api ->
            val key = "${api.httpMethodNm}-${api.urlPath}"
            apiCache[key] = api
        }
        log.info("API 원장 메모리 캐시 로딩 완료. (총 ${apiCache.size} 개)")
    }

    /**
     * 메모리 캐시 정보 반환 (신규 등록 시 조회 등)
     */
    fun getCachedApis(): Map<String, ApiEntity> {
        return apiCache.toMap()
    }

    /**
     * 동적 갱신이 필요할 경우 단건 업데이트
     */
    fun updateCache(api: ApiEntity) {
        val key = "${api.httpMethodNm}-${api.urlPath}"
        apiCache[key] = api
    }

    /**
     * API 호출 시 접근 가능한지(useYn = 'Y') 확인하고 정보(ApiEntity)를 반환
     */
    fun checkAndGetApi(httpMethod: String, pathPattern: String): ApiEntity? {
        val key = "$httpMethod-$pathPattern"
        val api = apiCache[key] ?: return null
        
        if (api.useYn == "N") {
            log.warn("사용 제한된 API 호출 시도 차단: {}", key)
            throw UnauthorizedException("현재 사용이 중지된 API 입니다. (사용여부: N)")
        }
        
        return api
    }
}
