package com.nsnm.herenow.fwk.custom.config


import com.nsnm.herenow.lib.ext.logger
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.ResourceHttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableScheduling
class BaseConfig(
    val env: Environment
) : WebMvcConfigurer {
    protected val log = logger()

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }



    @InitBinder
    fun allowEmptyDateBinding(binder: WebDataBinder) {
        binder.registerCustomEditor(String::class.java, StringTrimmerEditor(true))
    }

    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)

        return mapper
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val mapper = objectMapper().registerKotlinModule()

        val jacksonConverter = MappingJackson2HttpMessageConverter()
        jacksonConverter.objectMapper = mapper

        converters.add(jacksonConverter)
        converters.add(StringHttpMessageConverter())
        converters.add(ByteArrayHttpMessageConverter())
        converters.add(ResourceHttpMessageConverter())

        super.configureMessageConverters(converters)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 허용
            .allowedOrigins("*") // 모든 도메인 허용
            .allowedMethods("*") // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등)
            .allowedHeaders("*") // TEXT모든 헤더 허용
            .allowCredentials(false) // 인증 정보는 비활성화
            .maxAge(3600) // 프리플라이트 요청의 캐시 시간 (초 단위)
    }

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
    }

    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setDefaultTimeout(1000 * 60 * 60 * 12)
    }
}
