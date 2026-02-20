package com.nsnm.herenow.lib.utils

import java.net.InetAddress
import java.net.UnknownHostException

/**
 * 문자열이 숫자로만 구성되어 있는지 확인하는 확장 함수
 */
fun String.isNumeric(): Boolean {
    return this.all { it.isDigit() }
}

object StringUtils {
    /**
     * 문자열을 지정된 길이만큼 왼쪽에서 잘라냅니다.
     */
    fun left(input: String, length: Int): String {
        return if (input.length <= length) {
            input
        } else {
            input.substring(0, length)
        }
    }

    /**
     * 문자열을 지정된 길이만큼 오른쪽에서 잘라냅니다.
     */
    fun right(input: String, length: Int): String {
        return if (input.length <= length) {
            input
        } else {
            input.substring(input.length - length)
        }
    }

    /**
     * 문자열을 지정된 길이로 왼쪽에 패딩 문자를 추가합니다.
     */
    fun leftPad(input: String, totalLength: Int, padChar: String): String {
        if (padChar.length != 1) throw IllegalArgumentException("padChar must be a single character")
        if (input.length >= totalLength) return input

        val padLength = totalLength - input.length
        return padChar.repeat(padLength) + input
    }

    /**
     * IPv4 로 변환. 실패 시 그대로 리턴
     */
    fun convertToIPv4(ip: String): String {
        return try {
            val address = InetAddress.getByName(ip)
            // IPv6 로컬호스트 주소를 IPv4로 변환
            if (address.isLoopbackAddress) {
                "127.0.0.1"
            } else if (address.address.size == 4) {
                // IPv4 주소 반환
                address.hostAddress
            } else {
                // IPv6에서 IPv4로 매핑된 경우
                val ipv6 = address.hostAddress
                if (ipv6.contains(":") && ipv6.contains(".")) {
                    ipv6.substringAfterLast(":") // IPv4 부분 추출
                } else {
                    ipv6 // 그대로 IPv6 반환
                }
            }
        } catch (e: UnknownHostException) {
            // 유효하지 않은 IP인 경우 원본 반환
            ip
        }
    }
}