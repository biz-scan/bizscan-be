package com.umc9th.bizscan.global.config.swagger;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String securitySchemeName = "JWT TOKEN";

    @Bean
    public GroupedOpenApi allApi(ApiErrorCodeOperationCustomizer apiErrorCodeOperationCustomizer) {
        return GroupedOpenApi.builder()
                .group("BizScan-API-v1")
                .pathsToMatch("/**")
                .displayName("BizScan API 명세서")
                .addOpenApiCustomizer(createOpenApiCustomizer("BizScan API", "v0.1"))
                .addOperationCustomizer(apiErrorCodeOperationCustomizer)
                .build();
    }

    /**
     * OpenAPI 객체의 공통 설정을 담당하는 커스텀 로직
     */
    private OpenApiCustomizer createOpenApiCustomizer(String title, String version) {
        return openApi -> {
            openApi.info(new Info().title(title).version(version).description("BizScan API Swagger 명세서입니다."));
            openApi.setServers(List.of(
                    new Server().url("/")
            ));
            openApi.addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
            openApi.schemaRequirement(securitySchemeName, createBearerAuthScheme());
        };
    }

    /**
     * JWT Bearer 인증 스키마 생성
     */
    private SecurityScheme createBearerAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(securitySchemeName);
    }
}