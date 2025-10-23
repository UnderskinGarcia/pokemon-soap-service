package com.bankaya.pokemon.infrastructure.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import lombok.extern.log4j.Log4j2;

/**
 * OpenAPI/Swagger Configuration
 * Documents REST API and provides comprehensive information about SOAP service
 * API description is loaded from external markdown file for better maintainability
 */
@Log4j2
@Configuration
public class OpenApiConfig {

    public static final String FALLBACK_DESCRIPTION = """
            # Pokemon Service API
            
            This service provides access to Pokemon data through SOAP and REST interfaces.
            
            **WSDL:** `/pokemon/ws/pokemon.wsdl`
            
            **SOAP Endpoint:** `/pokemon/ws`
            
            For detailed documentation, please check the external resources.
            """;

    @Value("${server.port}")
    private String serverPort;

    @Value("${springdoc.api-description-file}")
    private String apiDescriptionFile;

    @Bean
    public OpenAPI pokemonServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pokemon Service - SOAP & REST API")
                        .description(loadApiDescription())
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bankaya Team")
                                .email("support@bankaya.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("📄 SOAP WSDL - Click here to download WSDL")
                        .url("/pokemon/ws/pokemon.wsdl"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("/")
                                .description("Current Server")
                ));
    }

    /**
     * Loads API description from markdown file in classpath
     * Falls back to default description if file cannot be loaded
     *
     * @return API description in markdown format
     */
    private String loadApiDescription() {
        try {
            ClassPathResource resource = new ClassPathResource(apiDescriptionFile);
            String description = resource.getContentAsString(StandardCharsets.UTF_8);
            log.info("Successfully loaded API description from: {}", apiDescriptionFile);
            return description;
        } catch (IOException e) {
            log.warn("Could not load API description file: {}. Using fallback description.", apiDescriptionFile, e);
            return getFallbackDescription();
        }
    }

    /**
     * Fallback description in case the markdown file cannot be loaded
     *
     * @return Simple fallback description
     */
    private String getFallbackDescription() {
        return FALLBACK_DESCRIPTION;
    }
}
