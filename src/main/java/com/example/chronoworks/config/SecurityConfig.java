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
                        // Permitir acceso a los archivos HTML y estáticos
                        .requestMatchers(
                                "/admin.html",
                                "/agregarAsignacion.html",
                                "/lider.html",
                                "/index.html",
                                "/agente.html",
                                "/login.html",
                                "/access-denied.html",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Permitir login y endpoints públicos
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/public/**").permitAll()

                        // Requiere roles específicos
                        .requestMatchers("/api/Admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/Lider/**").hasRole("LIDER")
                        .requestMatchers("/api/Agente/**").hasRole("AGENTE")

                        // Todo lo demás necesita autenticación
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
