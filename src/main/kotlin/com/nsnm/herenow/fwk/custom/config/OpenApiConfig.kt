package com.nsnm.herenow.fwk.custom.config

import com.nsnm.herenow.fwk.custom.model.pojo.DefaultExceptionResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.jackson.TypeNameResolver
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.servers.Server
import jakarta.annotation.PostConstruct
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class OpenApiConfig(
    val env: Environment,
    val ctx: ApplicationContext,
    val objectMapper: ObjectMapper
) : WebMvcConfigurer {

    @PostConstruct
    fun init() {
        val innerClassAwareTypeNameResolver = object : TypeNameResolver() {
            override fun getNameOfClass(cls: Class<*>): String {
                return cls.name.substringAfterLast('.').replace('$', '.')
            }
        }

        ModelConverters.getInstance().addConverter(ModelResolver(objectMapper, innerClassAwareTypeNameResolver))
    }

    @Bean
    fun customOpenApi(): OpenAPI {
        val isLocal = isLocal()

        var url = ""
        var description = "로컬"

        if (!isLocal) {
            url = ""
            description = "개발계"
        }

        val openApi = OpenAPI().info(
            Info()
                .title("HereNow")
                .description("물건 관리 시스템")
        )
            .addServersItem(Server().url(url).description(description))
            .components(
                Components()
                    .addSchemas(
                        "DefaultExceptionResponse",
                        Schema<Any>()
                            .addProperty("messageCode", StringSchema().example("HNW9999"))
                            .addProperty("message", StringSchema().example("Internal Server Error"))
                            .addProperty("type", StringSchema().example("S"))
                            .addProperty("classType", StringSchema().example("com.example.Exception"))
                            .addProperty(
                                "stack", Schema<List<Any>>() // Array 타입 정의
                                    .items(
                                        Schema<Any>().addProperty("code", StringSchema())
                                            .addProperty("message", StringSchema())
                                    )
                            )
                    )
                    .addHeaders("x-herenow-api", Header().description("API ID").schema(StringSchema()))
                    .addHeaders("x-herenow-guid", Header().description("guid").schema(StringSchema()))
                    .addHeaders(
                        "x-herenow-crypto", Header().description("Encrypted Y/N").schema(StringSchema())
                    )
            )
        return openApi
    }

    fun isLocal(): Boolean {
        return when {
            "prd" in env.activeProfiles -> false
            "test" in env.activeProfiles -> false
            "dev" in env.activeProfiles -> false
            "local" in env.activeProfiles -> true
            else -> true
        }
    }

    @Bean
    fun customize(openApi: OpenAPI): OperationCustomizer {
        return OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod? ->
            // 모든 콘트롤러 응답에 500 에러와 DefaultExceptionResponse 객체 지정
            operation.responses.addApiResponse(
                "500", ApiResponse().description("Internal Server Error").content(
                    Content().addMediaType(
                        "application/json",
                        MediaType().schema(Schema<DefaultExceptionResponse>().`$ref`("#/components/schemas/DefaultExceptionResponse"))
                    )
                )
            )
            operation
        }
    }

}
