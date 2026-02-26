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

        // Spring Security 인증 정보 연동
        val authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated && authentication.name != "anonymousUser") {
            ca.isLogin = true
            context.user.userId = authentication.name
            context.user.isAuthenticated = true
            
            // JWT 토큰인 경우 Claims 추출하여 공통부(ComUser) 보강
            if (authentication is org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken) {
                val claims = authentication.token.claims
                context.user.userNm = (claims["name"] ?: claims["email"] ?: claims["preferred_username"] ?: "").toString()
                context.user.userRole = (claims["role"] ?: claims["user_role"] ?: "").toString()
                context.user.authToken = authentication.token.tokenValue
            }
        } else {
            ca.isLogin = false
            context.user.isAuthenticated = false
        }

        context.headers = req.headerNames.toList().filterNot { it.equals("cookie", true) || it.equals("bot", true) }
            .associateWith { it ->
                req.getHeader(it).let { it -> if (it.equals("authorization", true)) "[masked]" else it!!.lowercase() }
            }
            .toMutableMap()
    }
}
