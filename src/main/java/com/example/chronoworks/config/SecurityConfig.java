package com.example.chronoworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
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
                        .requestMatchers("/login.html", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/validate").authenticated()

                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Restringir páginas por roles
                        .requestMatchers("/admin.html").hasRole("ADMIN")
                        .requestMatchers("/agente.html").hasRole("AGENTE")
                        .requestMatchers("/lider.html").hasRole("LIDER")

                        // Requiere roles específicos
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/Admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/Lider/**").hasRole("LIDER")
                        .requestMatchers("/api/Agente/**").hasRole("AGENTE")

                        // Todo lo demás necesita autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable()) // Deshabilitar form login tradicional
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api")) {
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                            } else {
                                response.sendRedirect("/login.html");
                            }
                        })
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
