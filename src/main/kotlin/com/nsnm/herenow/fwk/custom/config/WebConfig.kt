package com.nsnm.herenow.fwk.custom.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 서버 실행 경로 기준 app-builds 폴더를 /app-builds/** 로 웹 서빙
        val appBuildsPath = Paths.get("app-builds").toAbsolutePath().toUri().toString()
        registry.addResourceHandler("/app-builds/**")
            .addResourceLocations(appBuildsPath)
    }
}
