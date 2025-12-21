package com.takealook

import com.takealook.auth.component.HEADER_STRING
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = HEADER_STRING
        val securityRequirement = SecurityRequirement().addList(securitySchemeName)
        val components = Components().addSecuritySchemes(
            securitySchemeName,
            SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.APIKEY)
                .`in`(SecurityScheme.In.HEADER)
        )

        return OpenAPI()
            .info(
                Info()
                    .title("Takealook API")
                    .description("Takealook Backend API Documentation")
                    .version("1.0.0")
            )
            .addServersItem(Server().url("/").description("Default Server"))
            .addSecurityItem(securityRequirement)
            .components(components)
    }
}
