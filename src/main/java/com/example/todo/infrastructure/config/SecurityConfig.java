package com.example.todo.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig
 *
 * Configuration de la sécurité Spring Security avec JWT.
 * Définit les règles d'autorisation et la configuration CORS.
 *
 * @author Todo Team
 */
@Configuration
@EnableWebSecurity
@Profile("!local")
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()

                // Endpoints d'administration
                .requestMatchers("/actuator/**").hasRole("ADMIN")

                // Endpoints API - authentification requise
                .requestMatchers(HttpMethod.GET, "/api/v1/todos/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/todos/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/todos/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/todos/**").authenticated()

                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            );

        // TODO: Ajouter le JWT filter quand l'authentification sera implémentée
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines autorisées (configurables via application.yml)
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",  // React dev
            "http://localhost:4200",  // Angular dev
            "https://*.example.com"   // Production domains
        ));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(List.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        ));

        // Headers autorisés
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Cache-Control",
            "X-File-Name"
        ));

        // Headers exposés au client
        configuration.setExposedHeaders(List.of(
            "Authorization",
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 heure

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
