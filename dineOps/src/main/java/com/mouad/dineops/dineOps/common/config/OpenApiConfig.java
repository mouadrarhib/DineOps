package com.mouad.dineops.dineOps.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI dineOpsOpenApi() {
		final String bearerSchemeName = "BearerAuth";

		return new OpenAPI()
				.info(new Info()
						.title("DineOps API")
						.description("Multi-branch restaurant operations backend APIs")
						.version("v1")
						.contact(new Contact().name("DineOps Team")))
				.addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
				.schemaRequirement(
						bearerSchemeName,
						new SecurityScheme()
								.name(bearerSchemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT"));
	}
}
