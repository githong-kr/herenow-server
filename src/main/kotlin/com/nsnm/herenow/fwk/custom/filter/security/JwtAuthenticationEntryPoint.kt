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

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val unAuthException = UnauthorizedException("인증 정보가 올바르지 않거나 권한이 없습니다.")

        val errorResponse = DefaultExceptionResponse(
            messageCode = unAuthException.msgCd,
            message = unAuthException.message,
            type = "B",
            classType = unAuthException.javaClass.simpleName,
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
