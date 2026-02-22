package com.nsnm.herenow.fwk.custom.filter.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.nsnm.herenow.fwk.core.error.UnauthorizedException
import com.nsnm.herenow.fwk.custom.model.pojo.DefaultExceptionResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

import org.slf4j.LoggerFactory
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val authHeader = request.getHeader("Authorization")
        val shortAuth = authHeader?.take(20)?.plus("...") ?: "null"
        log.warn("Unauthorized access attempt from ${request.remoteAddr} | URI: [${request.method}] ${request.requestURI} | Auth Header: $shortAuth | Reason: ${authException.message}")

        val unAuthException = UnauthorizedException("인증 정보가 올바르지 않거나 권한이 없습니다.")

        val errorResponse = DefaultExceptionResponse(
            messageCode = "UNAUTHORIZED",
            message = unAuthException.msgCd,
            type = "B",
            classType = authException.javaClass.simpleName,
            serviceName = "Spring Security Filter",
            lineNumber = 0,
            methodName = "commence"
        )

        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
