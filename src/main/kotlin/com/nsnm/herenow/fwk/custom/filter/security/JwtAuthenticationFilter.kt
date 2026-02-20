package com.nsnm.herenow.fwk.custom.filter.security

import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val env: Environment,
    private val jwtValidator: JwtValidator
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. 헤더에서 토큰 추출 (보통 Authorization: Bearer <token>)
        val authorizationHeader = request.getHeader("Authorization")
        
        // 2. 현재 활성화된 프로필(환경) 확인
        val isLocal = env.activeProfiles.contains("local") || env.activeProfiles.isEmpty()

        if (isLocal) {
            // [로컬 환경]
            // 편의를 위해 Header 값이 없거나 임의의 값이 와도 통과하게 바이패스하거나, 
            // 프론트엔드 작업 시 번거롭지 않도록 기본 유저로 세팅합니다.
            log.debug("[LOCAL Env] Strict token validation bypassed.")
            
            // 토큰이 있다면 가져오고, 없다면 기본 테스트 사용자 ID 사용
            val userId = if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                "LOCAL_TOKEN_USER"
            } else {
                "LOCAL_TEST_USER"
            }
            
            setAuthentication(userId, "ROLE_USER")

        } else {
            // [테스트/운영 환경]
            // Expo 앱에서 넘겨준 실제 OAuth 로그인/JWT 토큰의 철저한 유효성 판별 로직이 들어가야 하는 곳
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                val token = authorizationHeader.substring(7)
                log.debug("[TEST/PROD Env] Token received for validation.")

                // jjwt 라이브러리를 통한 Supabase Secret 기반 검증
                val claims = jwtValidator.validateAndGetClaims(token)

                if (claims != null) {
                    // Supabase JWT의 경우 식별자가 'sub' (subject) 에 들어있습니다.
                    val extractedUid = claims.subject ?: "UNKNOWN_UID"
                    setAuthentication(extractedUid, "ROLE_USER")
                } else {
                    log.warn("[TEST/PROD Env] Invalid token.")
                    // SecurityContext에 아무것도 세팅하지 않아 401(UnauthorizedEntryPoint) 발생 유도
                }
            } else {
                log.warn("[TEST/PROD Env] No Authorization header provided.")
            }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Spring Security Context에 인증 토큰을 심어주는 공통 함수
     */
    private fun setAuthentication(userId: String, role: String) {
        val authorities = listOf(SimpleGrantedAuthority(role))
        val authToken = UsernamePasswordAuthenticationToken(userId, null, authorities)
        
        SecurityContextHolder.getContext().authentication = authToken
        
        // (선택 사항) CustomContextHolder에 사용자 고유 식별자 임시 보관
        // CustomContextHolder.getContext().com.userNm = userId
        // CustomContextHolder.getContext().com.guid = userId
    }
}
