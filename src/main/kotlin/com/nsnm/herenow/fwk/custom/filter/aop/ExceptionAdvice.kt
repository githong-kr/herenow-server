package com.nsnm.herenow.fwk.custom.filter.aop

import com.nsnm.herenow.fwk.core.base.BaseException
import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import com.nsnm.herenow.fwk.core.error.BizException
import com.nsnm.herenow.fwk.core.error.UnauthorizedException
import com.nsnm.herenow.fwk.custom.model.pojo.DefaultException
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
            else -> log.error(exception.message, exception) // TODO need added other logger
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

            // 최초 에러정보 loop in cause
            response.classType = exception.javaClass.simpleName
            var cause: Throwable = exception
            while (cause.cause != null) {
                if (cause.cause is BaseException) {
                    val innerCause = cause.cause as BaseException
                    val msgCtn = ErrorCode.getMessage(innerCause)
                    response.stack.add(DefaultException(innerCause.msgCd, msgCtn))

                    // 에러 유형은 최초 발생한 에러로 재지정
                    response.type = when (exception) {
                        is UnauthorizedException -> "B"
                        is BizException -> "B"
                        is BaseException -> "D"
//                    is BadSqlGrammarException -> "D"
                        else -> "S"
                    }

                } else {
                    response.stack.add(
                        DefaultException(
                            cause.cause!!.localizedMessage,
                            cause.cause!!.message.toString()
                        )
                    ) // TODO 230215 | need check
                }
                cause = cause.cause!!
            }

            // 에러 라인 정보
            response.serviceName = cause.stackTrace[0].className
            response.methodName = cause.stackTrace[0].methodName
            response.lineNumber = cause.stackTrace[0].lineNumber

            return ResponseEntity.status(status).body(response)
        }

    }
}
