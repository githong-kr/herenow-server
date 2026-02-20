package com.nsnm.herenow.fwk.custom.filter.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtValidator(
    @Value("\${herenow.jwt.secret:}") private val secretKey: String
) {
    private val log = LoggerFactory.getLogger(JwtValidator::class.java)

    /**
     * 서명된 SecretKey 객체 반환
     */
    private fun getSigningKey(): SecretKey? {
        if (secretKey.isBlank()) return null
        return try {
            val keyBytes = Decoders.BASE64.decode(secretKey)
            Keys.hmacShaKeyFor(keyBytes)
        } catch (e: Exception) {
            log.warn("Invalid JWT Secret Key format: {}", e.message)
            null
        }
    }

    /**
     * 토큰 검증 및 Claims(내용) 파싱
     * Supabase 토큰의 경우 sub 필드에 User 고유 식별자(UUID)가 담김
     */
    fun validateAndGetClaims(token: String): Claims? {
        val key = getSigningKey()
        if (key == null) {
            log.error("JWT Secret Key is not configured properly.")
            return null
        }

        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            log.error("Token validation failed: {}", e.message)
            null
        }
    }
}
