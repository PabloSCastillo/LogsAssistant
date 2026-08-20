package com.dicsys.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar anotaciones como @PreAuthorize en metodos/servicios
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitado para APIs Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos o de integración interna (ej. Webhooks con token de Slack)
                        .requestMatchers("/api/v1/slack/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html")
                        .permitAll()
                        // Permitir acceso público temporal al endpoint del chat
                        .requestMatchers("/api/v1/chat/**").permitAll()
                        // Endpoints que requieren roles específicos
                        .requestMatchers("/api/v1/chat/query")
                        .hasAnyRole("ANALISTA_FUNCIONAL", "SOPORTE", "ADMIN")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated());

        // Aquí se concatenaría el filtro de validación de Tokens JWT o OAuth2
        // http.addFilterBefore(jwtAuthenticationFilter,
        // UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
