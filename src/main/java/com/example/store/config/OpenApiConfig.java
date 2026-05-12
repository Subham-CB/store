package com.example.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Store API")
                        .description("A simple store API")
                        .version("1.0")
                        .contact(new Contact()
                                .name("SecuritEase Dev")
                                .url("https://www.securitease.com")
                                .email("internal@securitease.com"))
                        .termsOfService("https://www.securitease.com")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.htm")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Development server")));
    }
}
