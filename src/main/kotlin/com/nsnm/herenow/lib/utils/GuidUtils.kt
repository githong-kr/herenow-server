package com.nsnm.herenow.lib.utils

import com.nsnm.herenow.fwk.core.error.FwkException
import com.nsnm.herenow.lib.ext.logger
import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Component
class GuidUtils(
    val ctx: ApplicationContext,
    val env: Environment
) {
    companion object {
        lateinit var instance: GuidUtils
        val log = logger()
        val hostName: String
            get() = instance.ctx.getBean(GuidUtils::class.java).getServerId()

        fun generate(): String = instance.ctx.getBean(GuidUtils::class.java).generate()
        fun generateNextHop(guid: String): String = instance.ctx.getBean(GuidUtils::class.java).generateNextHop(guid)
        fun generateNextForMci(guid: String): String =
            instance.ctx.getBean(GuidUtils::class.java).generateNextForMci(guid)
    }

    val counter = AtomicInteger()

    @PostConstruct
    fun init() {
        instance = this
    }

    @Synchronized
    fun generate(): String {
        val sb = StringBuilder()
        sb.append(DateUtils.currentDt)      // 일자(8)
        sb.append(DateUtils.currentHms)     // 시분초(6)
        sb.append("001")                    // 그룹사코드(3)
        sb.append("HNW")                    // 채널코드(3)
        sb.append(getServerId())            // 채널ID(7)
        sb.append(nextVal())                // 일련번호(6)
        sb.append("001")                    // hop(3)

        return sb.toString()
    }

    fun generateNextHop(guid: String): String {
        val result: String

        if (guid.length == 36) {
            if (guid.substring(33, 36).isNumeric()) {
                result = listOf(
                    guid.substring(0, 33),
                    StringUtils.leftPad((guid.substring(33, 36).toInt() + 1).toString(), 3, "0"),
                ).joinToString("")
            } else {
                throw FwkException("DPM9001", listOf(guid.substring(33, 36)))
            }

        } else {
            throw FwkException("DPM9002", listOf(guid.length.toString()))
        }

        return result
    }

    fun generateNextForMci(guid: String): String {
        val result: String

        if (guid.length == 36) {
            if (guid.substring(33).isNumeric()) {
                result = listOf(
                    guid.substring(0, 27),
                    nextVal(),
                    guid.substring(33)
                ).joinToString("")
            } else {
                throw FwkException("DPM9001", listOf(guid.substring(33)))
            }

        } else {
            throw FwkException("DPM9002", listOf(guid.length.toString()))
        }

        return result

    }

    fun getServerId(batch: Boolean? = false): String {
        val hostNm = try {
            // hostname 으로부터 '-' 와 '.'을 제거
            InetAddress.getLocalHost().hostName.replace("-", "").replace(".", "")
        } catch (e: UnknownHostException) {
            "unknown"
        }

        val serverId = if (env.activeProfiles.contains("local")) {
            StringUtils.leftPad(StringUtils.left(hostNm, 6), 6, "0")
        } else {
            StringUtils.leftPad(StringUtils.right(System.getProperty("application.name", hostNm), 6), 6, "0")
        }

        return (if (batch == true) "B" else "O") + serverId.uppercase(Locale.getDefault())
    }

    fun nextVal(): String {
        counter.getAndUpdate { if (it >= 999999) 0 else it + 1 }
        return StringUtils.leftPad(counter.toString(), 6, "0")
    }
}
