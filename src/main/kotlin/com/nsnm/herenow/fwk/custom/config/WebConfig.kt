package com.nsnm.herenow.fwk.custom.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 서버 실행 경로(컨테이너 /app) 기준 app-builds 폴더를 서빙
        registry.addResourceHandler("/app-builds/**")
            .addResourceLocations("file:app-builds/")
    }
}
