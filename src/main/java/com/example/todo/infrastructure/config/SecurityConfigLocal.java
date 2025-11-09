package com.example.todo.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Local security configuration used for development/testing when the `local` profile is active.
 *
 * This configuration permits access to API endpoints so you can test via Swagger / Postman
 * without a JWT implementation. It is intentionally lightweight and MUST NOT be used in
 * production.
 */
@Configuration
@EnableWebSecurity
@Profile("local")
public class SecurityConfigLocal {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow swagger, actuator and API access when running locally
                .requestMatchers("/api/v1/todos/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
