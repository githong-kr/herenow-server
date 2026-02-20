package com.nsnm.herenow.fwk.custom.filter.aop

import com.nsnm.herenow.fwk.core.base.BaseException
import com.nsnm.herenow.lib.ext.logger
import com.nsnm.herenow.lib.model.enum.ErrorCode
import com.nsnm.herenow.lib.utils.DateUtils
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.MDC
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Aspect
@Component
class ServiceAdvice {
    companion object {
        private const val mdcServiceName = "logServiceFileName"
    }

    private val log = logger()

    @Around(PointcutList.SERVICE_LAYER)
    fun aroundService(pjp: ProceedingJoinPoint): Any? {
        var returnValue: Any? = null
        val serviceFullName = (pjp.signature.declaringType.simpleName + "." + pjp.signature.name)
        val args = pjp.args.toList().joinToString(",")
        var elapsed: Long = 0

        val bfServiceName = MDC.get(mdcServiceName)
        MDC.put(
            mdcServiceName,
            "${pjp.signature.declaringType.simpleName}.log${DateUtils.currentDateTimeFormat(".yyMMdd")}"
        )

        val withArgs = if (args.isNotEmpty()) {
            if (args.length > 120) "with ${args.slice(0..120)}..."
            "with $args"
        } else ""
        log.info(" >>>>>  service start     [$serviceFullName()] $withArgs ")

        try {
            elapsed = measureTimeMillis {
                returnValue = pjp.proceed()
            }
        } catch (e: Exception) {
            var msgCtn = e.message
            if (e is BaseException) {
                msgCtn = ErrorCode.getMessage(e)
                msgCtn = e.msgCd + " " + msgCtn
            }

            log.error("     >  [$serviceFullName()] occurred error {${msgCtn}}]")

            throw e
        } finally {
            val returnForLog = when {
                returnValue != null && returnValue.toString().length > 120 -> "{ ${
                    returnValue.toString()
                        .slice(0..120)
                }...}"

                returnValue != null -> "{ $returnValue }"
                else -> ""
            }
            log.info(" >>>>>  service   end     [$serviceFullName()] [${elapsed}ms] $returnForLog")
            if (bfServiceName == null)
                MDC.remove(mdcServiceName)
            else
                MDC.put(mdcServiceName, bfServiceName)
        }

        return returnValue
    }
}
