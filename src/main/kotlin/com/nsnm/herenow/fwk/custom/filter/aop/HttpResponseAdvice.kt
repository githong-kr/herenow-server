package com.nsnm.herenow.fwk.custom.filter.aop

import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import com.nsnm.herenow.fwk.core.context.CustomContextHolder.clearContext
import com.nsnm.herenow.fwk.custom.model.pojo.DefaultResponse
import com.nsnm.herenow.lib.ext.logger
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.lang.Nullable
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

/**
 * 콘트롤러 출력 전 헤더 셋팅
 */
@ControllerAdvice
class HttpResponseAdvice : ResponseBodyAdvice<Any> {

    protected val log = logger()

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean =
        true

    override fun beforeBodyWrite(
        @Nullable body: Any?,
        returnType: MethodParameter,
        mediaType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {

        val uri = request.uri.path
        // Swagger 요청 필터링
        if (uri.startsWith("/auth") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            return body // Swagger 요청은 처리하지 않음
        }

        val context = CustomContextHolder.getContext()
        var returnValue: Any? = null

        response.headers.add("X-GUID", context.com.guid)
        response.headers.add("X-elapsed", context.com.elapsed.toString())
        response.headers.add("X-api", context.com.apiId.toString())

        returnValue = if (mediaType.isCompatibleWith(MediaType.APPLICATION_JSON) ||
            mediaType.isCompatibleWith(MediaType("application", "*+json"))
        ) {
            DefaultResponse.of(body, context)
        } else {
            body
        }

        clearContext()

        return returnValue

    }

}
