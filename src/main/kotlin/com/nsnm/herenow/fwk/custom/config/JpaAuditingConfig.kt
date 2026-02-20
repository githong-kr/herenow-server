package com.nsnm.herenow.fwk.custom.config

import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import java.util.*

@Configuration
class JpaAuditingConfig {

    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAware {
            // 현재 쓰레드의 CustomContext에서 GUID를 꺼내 리턴 (없을 경우 "SYSTEM" 등 폴백)
            val context = CustomContextHolder.getContext()
            val guid = context.com.guid
            Optional.of(guid.ifBlank { "SYSTEM" })
        }
    }
}
