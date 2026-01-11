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
                    .description("""
                        Takealook Backend API Documentation
                        
                        ## WebSocket 채팅 연결 가이드
                        
                        브라우저/모바일에서 WebSocket 채팅에 연결하려면:
                        
                        1. **티켓 발급**: `POST /chat/ticket` 호출 (accessToken 헤더 필요)
                        2. **WebSocket 연결**: `ws(s)://server/chat?ticket={발급받은_티켓}`
                        
                        ```
                        [클라이언트]                    [서버]
                            │                             │
                            │ POST /chat/ticket           │
                            │ Header: accessToken         │
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
