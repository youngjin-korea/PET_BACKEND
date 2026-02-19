package com.core.pet.backend.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api.pet-service.com", description = "운영 서버"),
                @Server(url = "http://localhost:8080", description = "로컬 서버")
        }
)
@Configuration
public class SwaggerConfig {

    private static final String BEARER_TOKEN_PREFIX = "bearer-key";

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(BEARER_TOKEN_PREFIX);

        return new OpenAPI()
                .info(new Info()
                        .title("Pet Service API")
                        .description("Pet Service Backend REST API 문서")
                        .version("v1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_TOKEN_PREFIX, securityScheme))
                .addSecurityItem(securityRequirement);
    }

    @Bean
    public GroupedOpenApi all() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/**")
                .packagesToScan("com.core.pet.backend")
                .build();
    }

    @Bean
    public GroupedOpenApi auth() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/**")
                .packagesToScan("com.core.pet.backend.auth")
                .build();
    }

    @Bean
    public GroupedOpenApi member() {
        return GroupedOpenApi.builder()
                .group("member")
                .pathsToMatch("/**")
                .packagesToScan("com.core.pet.backend.domain.member")
                .build();
    }

    @Bean
    public GroupedOpenApi board() {
        return GroupedOpenApi.builder()
                .group("board")
                .pathsToMatch("/**")
                .packagesToScan("com.core.pet.backend.domain.board")
                .build();
    }
}
