package com.joel.recipes.config.security;

import com.joel.recipes.model.RoleType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class FilterChainConfig {

    private final JwtAuthenticationConverter authenticationConverter;

    public FilterChainConfig(JwtAuthenticationConverter authenticationConverter) {
        this.authenticationConverter = authenticationConverter;
    }

    @Bean
    public SecurityFilterChain restApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/**")
                .csrf(AbstractHttpConfigurer::disable) // CSRF needs to be disabled since application is stateless
                .authorizeHttpRequests(auth -> {

                    auth
                            .requestMatchers("/restricted-url").hasRole(RoleType.USER.name())
                            .requestMatchers("/**").permitAll();

                })
                .oauth2ResourceServer(oauth -> oauth.jwt(jwtCustomizer -> jwtCustomizer.jwtAuthenticationConverter(authenticationConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}