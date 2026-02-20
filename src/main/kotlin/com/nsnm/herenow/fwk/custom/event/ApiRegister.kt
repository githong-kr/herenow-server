package com.nsnm.herenow.fwk.custom.event

import com.nsnm.herenow.fwk.custom.service.ApiRegistryService
import com.nsnm.herenow.lib.ext.logger
import com.nsnm.herenow.lib.model.entity.log.ApiEntity
import com.nsnm.herenow.lib.model.repository.log.ApiRepository
import io.swagger.v3.oas.annotations.Operation
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method
import kotlin.system.measureTimeMillis

@Component
class ApiRegister(
    private val requestMappingHandlerMapping: RequestMappingHandlerMapping,
    private val apiRepository: ApiRepository,
    private val apiRegistryService: ApiRegistryService
) {
    private val log = logger()

    @EventListener
    fun handleApplicationStarted(event: ApplicationStartedEvent) {
        log.info("========== API 원장 자동 구성 시작 ==========")
        val elapsedTime = measureTimeMillis {
            registerApis()
            // 저장된 최신 원장을 메모리(콜 검사용)에 적재
            apiRegistryService.initializeCache()
        }
        log.info("========== API 원장 자동 구성 완료 (${elapsedTime}ms) ==========")
    }

    private fun registerApis() {
        val skipPaths = listOf(
            "/v3/api-docs", "/v3/api-docs.yaml", "/swagger-ui.html", "/error"
        )
        // 기존 DB 데이터 전체 조회
        val existingApis = apiRepository.findAll()
            .associateBy { "${it.httpMethodNm}-${it.urlPath}" }
            .toMutableMap()

        val saveList = mutableListOf<ApiEntity>()

        requestMappingHandlerMapping.handlerMethods.forEach { (requestInfo, handlerMethod) ->
            // URL 문자열 추출 (여러 개일 경우 첫번째 것 사용 혹은 정규화)
            val urlPatterns = requestInfo.pathPatternsCondition?.patternValues ?: emptySet()
            if (urlPatterns.isEmpty()) return@forEach
            
            val url = urlPatterns.first()
            if (skipPaths.any { url.startsWith(it) }) return@forEach

            // Http Method 추출 (비어있으면 전체 허용이므로 임의 지정)
            val methods = if (requestInfo.methodsCondition.isEmpty) {
                listOf("GET", "POST", "PUT", "PATCH", "DELETE")
            } else {
                requestInfo.methodsCondition.methods.map { it.name }
            }

            methods.forEach { httpMethod ->
                val apiKey = "$httpMethod-$url"
                val methodObj: Method = handlerMethod.method
                val operationAnno = methodObj.getAnnotation(Operation::class.java)

                val summary = operationAnno?.summary ?: ""
                val desc = operationAnno?.description

                val existingEntity = existingApis[apiKey]
                if (existingEntity == null) {
                    // 신규 API
                    val newApi = ApiEntity(
                        httpMethodNm = httpMethod,
                        urlPath = url,
                        classNm = methodObj.declaringClass.simpleName,
                        methodNm = methodObj.name,
                        apiNm = summary,
                        apiDesc = desc,
                        useYn = "Y"
                    )
                    saveList.add(newApi)
                    log.info("신규 API 원장 자동 등록 대기: $apiKey")
                } else {
                    // 기존 API 갱신 체크
                    var needUpdate = false
                    if (existingEntity.apiNm != summary || existingEntity.classNm != methodObj.declaringClass.simpleName) {
                        existingEntity.apiNm = summary
                        existingEntity.apiDesc = desc
                        existingEntity.classNm = methodObj.declaringClass.simpleName
                        existingEntity.methodNm = methodObj.name
                        needUpdate = true
                    }
                    if (needUpdate) {
                        saveList.add(existingEntity)
                    }
                }
            }
        }

        if (saveList.isNotEmpty()) {
            apiRepository.saveAll(saveList)
            log.info("API 원장 총 ${saveList.size}개 등록 및 갱신 완료")
        }
    }
}
