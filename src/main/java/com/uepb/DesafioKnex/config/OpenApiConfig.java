package com.uepb.DesafioKnex.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Desafio Backend Knex",
                version = "0.0.1",
                description = """
            API de controle de estoque e ecommerce simplificado.
            
            Para acessar endpoints protegidos:
            1. Faça login em POST /auth/login
            2. Copie o token retornado
            3. Clique em "Authorize" e cole o token (sem o prefixo "Bearer ")
            """,
                contact = @Contact(name = "André Vinícius Barros Macambira")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}