package com.nsnm.herenow.fwk.core.context

import com.nsnm.herenow.lib.ext.logger
import com.nsnm.herenow.lib.utils.DateUtils
import com.nsnm.herenow.lib.utils.GuidUtils
import com.nsnm.herenow.lib.utils.StringUtils.convertToIPv4
import com.nsnm.herenow.lib.utils.UriUtils.extractBaseUrl
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.http.HttpMethod
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.InetAddress
import java.net.URLDecoder
import java.net.UnknownHostException
import java.time.OffsetDateTime
import java.time.ZoneId


object CustomContextUtil {

    private val props = OnceProps()
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
        val cu = context.user

        if (!props.isSetProps) {
            setCommonAreaOnce(env)
        } else {
            setCommonAreaFromStatic()
        }

        // context common
        ca.startDt = OffsetDateTime.now(ZoneId.of("+9"))
        ca.remoteIp = req.remoteAddr
        ca.trDt = DateUtils.currentDt
        ca.path = req.requestURI.trim()
        ca.method = req.method.uppercase()
        if (!req.queryString.isNullOrEmpty()) {
            ca.queryString = URLDecoder.decode(req.queryString, "UTF-8")
        }

        ca.pathPattern = ca.path
        ca.apiId = 0
        ca.apiNm = null
        ca.apiKey = ""

        ca.guid = GuidUtils.generate()
        ca.guidLast = ca.guid

        if (req.getHeader("referer") != null) {
            val referrer = req.getHeader("referer")
            ca.referrer = extractBaseUrl(referrer)
            if (referrer != null && referrer.startsWith("https")) ca.isHttps = true
        }

        var clientIp = req.getHeader("x-forwarded-for")
        clientIp = if (clientIp != null) {
            clientIp.split(",")[0]
        } else {
            req.remoteAddr
        }
        ca.remoteIp = convertToIPv4(clientIp)

        ca.isLogin = false
        log.warn("인증 제외 (임시)")

        // body
        if (req.method in arrayOf("POST", "PATCH", "DELETE")) { // TODO except file upload
            try {
                var body = req.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                if (body.isNotEmpty()) {
                if (body.length > 4000) {
                    body = body.substring(0..4000)
                }
                body = org.apache.commons.lang3.StringUtils.chomp(body)
                body = body.replace("\n", "")
                context.com.body = body
            }
            } catch (e: Exception) {
               log.warn("body read limit error: ${e.message}")
            }
        }

        context.headers = req.headerNames.toList().filterNot { it.equals("cookie", true) || it.equals("bot", true) }
            .associateWith { it ->
                req.getHeader(it).let { it -> if (it.equals("authorization", true)) "[masked]" else it!!.lowercase() }
            }
            .toMutableMap()

        // User 정보 설정
//        if (auth is DpmAuthentication) {
//
//            context.user = ComUser(
//                userId = auth.userId ?: 0,
//                userNm = auth.keyCloackInfo.nickName,
//                userNmHan = auth.keyCloackInfo.korNm,
//                deptNm = auth.keyCloackInfo.deptNm,
//                email = auth.keyCloackInfo.email,
//                roles = auth.roles.map { it.roleId },
//                isBizPlatformLogin = isBizPlatformLogin,
//                bizPlatformBridgeToken = bizPlatformBridgeToken ?: "",
//                isAdmin = isAdmin
//            )
//            context.com.userNm = auth.keyCloackInfo.nickName
//            context.com.isLogin = true
//            SqmNode.log.debug("사용자 로그인 O")
//        } else {
//            SqmNode.log.debug("사용자 로그인 X")
//        }

    }

    private fun setCommonAreaOnce(env: Environment) {
        props.appName = env["spring.application.name"].orEmpty()
        props.profile = env.activeProfiles.toString()

        try {
            props.hostNm = InetAddress.getLocalHost().hostName
        } catch (e: UnknownHostException) {
            props.hostNm = "unknown"
        }

        props.isPrd = env.activeProfiles.contains("prd")
        props.isTest = env.activeProfiles.contains("test")
        props.isDev = env.activeProfiles.contains("dev")
        props.isLocal = env.activeProfiles.contains("local")

        props.isSetProps = true

        setCommonAreaFromStatic()
    }

    private fun setCommonAreaFromStatic() {
        val ca = CustomContextHolder.getContext().com
        ca.appName = props.appName
        ca.profile = props.profile
        ca.hostNm = props.hostNm
        ca.isPrd = props.isPrd
        ca.isTest = props.isTest
        ca.isDev = props.isDev
        ca.isLocal = props.isLocal
    }

    data class OnceProps(
        var appName: String = "",
        var profile: String = "",
        var hostNm: String = "",
        var isPrd: Boolean = false,
        var isTest: Boolean = false,
        var isDev: Boolean = false,
        var isLocal: Boolean = false,
        var isSetProps: Boolean = false,
    )
}
