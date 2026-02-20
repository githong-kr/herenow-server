package com.nsnm.herenow.lib.utils

import java.net.URI

object UriUtils {
    fun extractBaseUrl(referrer: String): String {
        val uri = URI(referrer)
        val scheme = uri.scheme // 프로토콜 (http 또는 https)
        val host = uri.host // 호스트 (예: www.example.com)
        val port = if (uri.port != -1) ":${uri.port}" else "" // 포트 (없으면 빈 문자열)

        return "$scheme://$host$port" // 프로토콜 + 호스트 + 포트로 구성된 URL 반환
    }
}
