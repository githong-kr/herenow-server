package com.nsnm.herenow.fwk.custom.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val env: Environment,
    private val jwtAuthenticationEntryPoint: com.nsnm.herenow.fwk.custom.filter.security.JwtAuthenticationEntryPoint
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val isLocal = env.activeProfiles.contains("local") || env.activeProfiles.isEmpty()
        http
            .httpBasic { it.disable() } // 기본 HttpBasic 인증 비활성화
            .csrf { it.disable() } // REST API 이므로 CSRF 비활성화
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // JWT(혹은 자체 토큰) 방식이므로 Stateless 세팅
            .authorizeHttpRequests {
                if (isLocal) {
                    it.anyRequest().permitAll()
                    http.addFilterBefore(object : org.springframework.web.filter.OncePerRequestFilter() {
                        override fun doFilterInternal(
                            request: jakarta.servlet.http.HttpServletRequest,
                            response: jakarta.servlet.http.HttpServletResponse,
                            filterChain: jakarta.servlet.FilterChain
                        ) {
                            val token = org.springframework.security.authentication.UsernamePasswordAuthenticationToken("dummy-uid-1234", null, listOf())
                            org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = token
                            filterChain.doFilter(request, response)
                        }
                    }, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter::class.java)
                } else {
                    // 기본 정적 및 Swagger 페이지, 헬스체크 허용, 나머지는 무조건 인증!
                    it.requestMatchers("/", "/actuator/health", "/api/v1/sample/**", "/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**", "/v3/api-docs.yaml")
                        .permitAll()
                        .anyRequest().authenticated()
                }
            }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            // H2 Console iFrame 접속 허용
            .headers { headers -> headers.frameOptions { it.disable() } }

        if (!isLocal) {
            // Supabase Oauth2 리소스 서버 설정 (운영/테스트 등 원격 환경에서만 진짜 토큰 검증 필터 활성화)
            http.oauth2ResourceServer { oauth2 ->
                oauth2.jwt {}
                oauth2.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }
        }

        return http.build()
    }

    @Bean
    fun jwtDecoder(): org.springframework.security.oauth2.jwt.JwtDecoder {
        val jwkSetUri = env.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri") 
            ?: throw IllegalStateException("jwk-set-uri is required for JWT validation")
        
        return org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .jwsAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.ES256)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*") // 모든 패턴 우선 허용 (필요 시 모바일 앱/EAS 주소만 개방)
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
