package com.ciyocloud.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Swagger 文档配置
 *
 * @author Lion Li
 */
@Configuration
public class SpringDocConfig {


    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info().title("Tduck API").description("Tduck API 演示").version("v1.0.0"))
                .externalDocs(new ExternalDocumentation().description("TduckX 服务").url("https://www.CIYOcloud.com/"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme().name(SECURITY_SCHEME_NAME).type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer").bearerFormat("JWT")));
    }


}
