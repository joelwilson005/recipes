package com.joel.recipes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ContentConfig {
    @Bean
    public WebMvcConfigurer defaultContentType() {
        return new WebMvcConfigurer() {
            @Override
            public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//                configurer.favorParameter(true)
//                        .parameterName("mediaType")
//                        .ignoreAcceptHeader(true)
//                        .useRegisteredExtensionsOnly(false)
//                        .defaultContentType(MediaType.APPLICATION_JSON)
//                        .mediaType("xml", MediaType.APPLICATION_XML)
//                        .mediaType("json", MediaType.APPLICATION_JSON);
            }
        };
    }
}