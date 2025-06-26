package com.example.chronoworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Archivos estáticos y HTML
                        .requestMatchers(
                                "/asignacion/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/**/*.html",
                                "/favicon.ico"
                        ).permitAll()

                        // API pública o login
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/public/**").permitAll()

                        // Rutas con roles
                        .requestMatchers("/api/Admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/Lider/**").hasRole("LIDER")
                        .requestMatchers("/api/Agente/**").hasRole("AGENTE")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()) // este activa el login HTTP
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // CAMBIA ESTO
                );

        return http.build();
    }
}
