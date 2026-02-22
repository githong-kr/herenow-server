package com.nsnm.herenow.fwk.custom.filter.aop

import com.nsnm.herenow.fwk.core.base.BaseException
import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import com.nsnm.herenow.fwk.core.error.BizException
import com.nsnm.herenow.fwk.core.error.UnauthorizedException
import com.nsnm.herenow.fwk.custom.model.pojo.DefaultExceptionResponse
import com.nsnm.herenow.lib.ext.logger
import com.nsnm.herenow.lib.model.enum.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
class ExceptionAdvice {
    private val log = logger()

    @ExceptionHandler(value = [NoHandlerFoundException::class, Exception::class])
    fun handleException(exception: Exception, request: HttpServletRequest): ResponseEntity<DefaultExceptionResponse>? {

        val context = CustomContextHolder.getContext()
        var status = HttpStatus.INTERNAL_SERVER_ERROR
        context.com.statCd = HttpStatus.INTERNAL_SERVER_ERROR.value().toString()

        when (exception) {
            is BaseException -> status = exception.httpStatus
            is NoHandlerFoundException -> status = HttpStatus.NOT_FOUND
            else -> {
                log.error("[UNHANDLED_EXCEPTION] reqGuid: ${context.com.guid}, message: ${exception.message}", exception) 
                // 외부 알림(Slack/Discord Webhook 등) 연동 포인트
            }

        if (status === HttpStatus.NOT_FOUND) {
            context.com.statCd = status.value().toString()
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } else {

            // build response
            val response = DefaultExceptionResponse()
            response.messageCode = exception.message

            // 에러 유형 지정
            response.type = when (exception) {
                is BizException -> "B"
                is BaseException -> "D"
//            is BadSqlGrammarException -> "D"
                is ConstraintViolationException -> "D"
                else -> "S"
            }

            // 에러 메시지 코드 변환
            if (exception is BaseException)
                response.message = ErrorCode.getMessage(exception)
            else
                response.message = exception.message

            // 최초 에러정보 추적
            response.classType = exception.javaClass.simpleName
            
            val cause: Throwable = exception

            // 에러 라인 정보
            response.serviceName = cause.stackTrace[0].className
            response.methodName = cause.stackTrace[0].methodName
            response.lineNumber = cause.stackTrace[0].lineNumber

            return ResponseEntity.status(status).body(response)
        }

    }
}
