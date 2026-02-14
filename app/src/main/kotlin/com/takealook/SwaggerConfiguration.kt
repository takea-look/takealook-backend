package com.takealook

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
        val bearerSchemeName = "bearerAuth"
        val legacySchemeName = "accessToken" // legacy header (backward compatible)

        val securityRequirement = SecurityRequirement().addList(bearerSchemeName)

        val components = Components()
            .addSecuritySchemes(
                bearerSchemeName,
                SecurityScheme()
                    .name("Authorization")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
            .addSecuritySchemes(
                legacySchemeName,
                SecurityScheme()
                    .name(legacySchemeName)
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
            )

        return OpenAPI()
            .info(
                Info()
                    .title("Takealook API")
                    .description("""
                        Takealook Backend API Documentation

                        ## Auth

                        - 표준: `Authorization: Bearer <JWT>`
                        - 레거시(호환): `accessToken: <JWT>`

                        ## WebSocket 채팅 연결 가이드

                        브라우저/모바일에서 WebSocket 채팅에 연결하려면:

                        1. **티켓 발급**: `POST /chat/ticket` 호출 (Authorization Bearer 필요)
                        2. **WebSocket 연결**: `ws(s)://server/chat?ticket={발급받은_티켓}`

                        ```
                        [클라이언트]                    [서버]
                            │                             │
                            │ POST /chat/ticket           │
                            │ Authorization: Bearer ...   │
                            ├────────────────────────────→│
                            │                             │
                            │ { "ticket": "abc...",       │
                            │   "expiresIn": 30 }         │
                            │←────────────────────────────┤
                            │                             │
                            │ WS /chat?ticket=abc...      │
                            ├────────────────────────────→│
                            │        Connected            │
                            │←────────────────────────────┤
                        ```

                        > 티켓은 30초간 유효하며, 일회용입니다.
                    """.trimIndent())
                    .version("1.0.0")
            )
            .addServersItem(Server().url("/").description("Default Server"))
            .addSecurityItem(securityRequirement)
            .components(components)
    }
}
