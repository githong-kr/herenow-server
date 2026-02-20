package com.nsnm.herenow.fwk.custom.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val env: Environment,
    private val jwtAuthenticationFilter: com.nsnm.herenow.fwk.custom.filter.security.JwtAuthenticationFilter,
    private val jwtAuthenticationEntryPoint: com.nsnm.herenow.fwk.custom.filter.security.JwtAuthenticationEntryPoint
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .httpBasic { it.disable() } // 기본 HttpBasic 인증 비활성화 
            .csrf { it.disable() } // REST API 이므로 CSRF 비활성화
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // JWT(혹은 자체 토큰) 방식이므로 Stateless 세팅
            .authorizeHttpRequests {
                val isLocal = env.activeProfiles.contains("local") || env.activeProfiles.isEmpty()

                if (isLocal) {
                    // [로컬 환경] 모든 API 허가 (테스트 편리성)
                    it.anyRequest().permitAll()
                } else {
                    // [운영/테스트 환경] 기본 정적 및 Swagger 페이지 허용, 나머지는 인증
                    it.requestMatchers("/", "/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                      .anyRequest().authenticated()
                }
            }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            // H2 Console iFrame 접속 허용
            .headers { headers -> headers.frameOptions { it.disable() } }
            // JWT 검증 커스텀 필터를 UsernamePasswordAuthenticationFilter 이전에 등록
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
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
