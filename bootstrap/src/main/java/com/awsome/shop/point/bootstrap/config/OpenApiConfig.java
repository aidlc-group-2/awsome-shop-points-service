package com.awsome.shop.point.bootstrap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置类
 *
 * <p>配置 API 文档的基本信息、安全认证方案等。</p>
 *
 * <p>访问地址：</p>
 * <ul>
 *   <li>Swagger UI: http://localhost:8080/swagger-ui.html</li>
 *   <li>OpenAPI JSON: http://localhost:8080/v3/api-docs</li>
 *   <li>OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-26
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * 配置 OpenAPI 文档
     *
     * @return OpenAPI 配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * API 基本信息
     */
    private Info apiInfo() {
        return new Info()
                .title("Awsome Shop Point Service API")
                .description("Awsome Shop Point Service API 文档\n\n" +
                        "## 认证方式\n\n" +
                        "本服务接口通过网关统一认证，请求已由网关完成身份验证。")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Dev Team")
                        .email("dev@example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    /**
     * 服务器配置
     */
    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("本地开发环境"),
                new Server()
                        .url("https://api.shop.example.com")
                        .description("生产环境")
        );
    }

    /**
     * 安全认证组件配置
     *
     * <p>本服务的认证由网关统一处理，此处配置仅用于 Swagger UI 测试时传递 Token。</p>
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("由网关统一认证，Token 通过网关传递"));
    }
}
