package com.example.chronoworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF para APIs
                .csrf(csrf -> csrf.disable())

                // 2. Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. REGLAS DE AUTORIZACIÓN - SIMPLIFICADAS
                .authorizeHttpRequests(auth -> auth
                        // ✅ PERMITIR TODOS los recursos estáticos y páginas HTML
                        .requestMatchers(
                                "/", "/index.html", "/login.html",
                                "/health", "/error", "/favicon.ico",
                                "/css/**", "/js/**", "/img/**", "/static/**"
                        ).permitAll()

                        // ✅ PERMITIR archivos HTML de las carpetas de roles
                        .requestMatchers("/admin/**", "/lider/**", "/agente/**").permitAll()

                        // ✅ PERMITIR APIs públicas
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/validate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/current-role").permitAll()

                        // ✅ PROTEGER APIs por rol
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/lider/**").hasRole("LIDER")
                        .requestMatchers("/api/agente/**").hasRole("AGENTE")

                        // ✅ Cualquier otra cosa requiere autenticación
                        .anyRequest().authenticated()
                )

                // 4. Deshabilitar form login
                .formLogin(form -> form.disable())

                // 5. Configurar logout
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, auth) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\": true}");
                        })
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )

                // 6. Configuración de sesiones para Railway
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )

                // 7. Manejo de excepciones - EVITAR BUCLE
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();
                            System.out.println("🔐 Authentication required for: " + uri);

                            // Para APIs, devolver JSON
                            if (uri.startsWith("/api/")) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\": \"Unauthorized\"}");
                            }
                            // Para páginas HTML, redirigir SOLO si no es login
                            else if (!uri.equals("/login.html") && !uri.endsWith(".html")) {
                                response.sendRedirect("/login.html");
                            } else {
                                // Si ya está en login, no redirigir
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            }
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",
                "https://chronoworks-production.up.railway.app"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}