package com.nsnm.herenow.fwk.core.context

import com.nsnm.herenow.lib.ext.logger
import com.nsnm.herenow.lib.utils.GuidUtils
import com.nsnm.herenow.lib.utils.StringUtils.convertToIPv4
import org.springframework.core.env.Environment
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.URLDecoder
import java.time.OffsetDateTime
import java.time.ZoneId


object CustomContextUtil {

    private val log = logger()

    fun initializeContext(
        env: Environment,
    ): CustomContext {
        val context = CustomContextHolder.getContext()
        setHttpRequestContext(context, env)
        return context
    }

    private fun setHttpRequestContext(
        context: CustomContext,
        env: Environment
    ) {
        val req = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val ca = context.com

        // context common
        ca.startDt = OffsetDateTime.now(ZoneId.of("+9"))
        ca.path = req.requestURI.trim()
        ca.method = req.method.uppercase()
        ca.pathPattern = ca.path
        ca.apiKey = ""
        ca.guid = GuidUtils.generate()

        var clientIp = req.getHeader("x-forwarded-for")
        clientIp = if (clientIp != null) {
            clientIp.split(",")[0]
        } else {
            req.remoteAddr
        }
        ca.remoteIp = convertToIPv4(clientIp)

        ca.isLogin = false
        log.warn("인증 제외 (임시)")

        context.headers = req.headerNames.toList().filterNot { it.equals("cookie", true) || it.equals("bot", true) }
            .associateWith { it ->
                req.getHeader(it).let { it -> if (it.equals("authorization", true)) "[masked]" else it!!.lowercase() }
            }
            .toMutableMap()
    }
}
