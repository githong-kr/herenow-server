package com.nsnm.herenow.fwk.custom.filter.aop

import com.nsnm.herenow.fwk.core.base.BaseException
import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import com.nsnm.herenow.fwk.core.context.CustomContextUtil.initializeContext
import com.nsnm.herenow.fwk.core.error.UnauthorizedException
import com.nsnm.herenow.lib.ext.logger
import com.nsnm.herenow.lib.utils.DateUtils
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.MDC
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.system.measureTimeMillis

@Aspect
@Component
class ControllerAdvice(
    private val env: Environment
) {
    private val log = logger()

    @Around(PointcutList.CONTROLLER_LAYER)
    fun aroundController(pjp: ProceedingJoinPoint): Any? {

        // Init --------------------------------------------------------------------------------------------------------
        var result: Any? = null
        val req = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val context = CustomContextHolder.getContext()
        val ca = context.com
        val signatureName = "${pjp.signature.declaringType.simpleName}.${pjp.signature.name}"


        initializeContext(env) // CommonArea 설정
        setMDC()

        // 권한 체크는 스프링 시큐리티 혹은 추후에 셋업
        


        // 실제 메서드 실행
        log.info(">>>>>  controller start [$signatureName() from [${req.remoteAddr}] by ${req.method} ${req.requestURI}[${ca.pathPattern}]")
        try {
            ca.elapsed = measureTimeMillis {
                result = pjp.proceed()
            }
            saveTransaction() // 거래내역 저장
        } catch (e: Exception) {
            log.info("[${ca.guid}] <<<<<  controller   end [$signatureName() from [${ca.remoteIp}] [${ca.elapsed}ms] with Error [${e.javaClass.simpleName}]")
            saveTransaction(e) // 거래내역 저장
            throw e
        } finally {
            log.info("<<<<<  controller   end [$signatureName() from [${ca.remoteIp}] [${ca.elapsed}ms]")
            setMDC(true)
        }

        return result // 원래 메서드 결과 반환
    }

    /**
     * MDC 값 셋팅
     */
    fun setMDC(bClear: Boolean = false) {
        val context = CustomContextHolder.getContext()
        val ca = context.com
        if (bClear) {
            MDC.put("guid", "")
            MDC.put("apiKey", "")
            MDC.put("userNm", "")
        } else {
            MDC.put("guid", ca.guid)
            MDC.put("apiKey", ca.apiKey)
            MDC.put("userNm", context.user?.userNm ?: "")
        }
    }

    /**
     * 거래내역 저장
     */
    fun saveTransaction(ex: Exception? = null) {
        val context = CustomContextHolder.getContext()
        val response =
            (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).response as HttpServletResponse
        val ca = context.com
        ca.err = ex
        ca.endDt = OffsetDateTime.now(ZoneId.of("+9"))
        ca.elapsed = Duration.between(ca.startDt, ca.endDt).toMillis()


        ca.statCd = response.status.toString()

        ex?.let {
            // error message
            val errorStack: ArrayList<String> = arrayListOf()
            var cause: Throwable = ex
            while (cause.cause != null) {
                errorStack.add(cause.cause!!.message.toString())
                cause = cause.cause!!
            }
            ca.errMsg = cause.message

            // status code
            if (ex is BaseException) ca.statCd = ex.httpStatus.value().toString()
            else ca.statCd = "500"
        }
    }
}
