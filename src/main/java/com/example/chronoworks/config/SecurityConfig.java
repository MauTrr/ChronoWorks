package com.example.chronoworks.config;

import com.example.chronoworks.service.EmpleadoService;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final EmpleadoService empleadoService;

    public SecurityConfig(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Archivos estáticos (permitir TODO lo de /static)
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/index.html/**",
                                "/login.html",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Endpoints públicos
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Validación de sesión
                        .requestMatchers("/api/auth/validate").authenticated()

                        //Permitir las rutas de forward desde VistaController
                        .requestMatchers("/admin/admin.html").hasRole("ADMIN")
                        .requestMatchers("/lider/lider.html").hasRole("LIDER")
                        .requestMatchers("/agente/agente.html").hasRole("AGENTE")

                        // Páginas restringidas por rol
                        .requestMatchers("/admin.html").hasRole("ADMIN")
                        .requestMatchers("/agente.html").hasRole("AGENTE")
                        .requestMatchers("/lider.html").hasRole("LIDER")

                        // APIs restringidas por rol
                        .requestMatchers("/api/Admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/lider/**").hasRole("LIDER")
                        .requestMatchers("/api/agente/**").hasRole("AGENTE")

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> {
                    logout.logoutUrl("/api/auth/logout");
                    logout.logoutSuccessHandler((request, response, auth) -> {
                        response.setStatus(HttpStatus.OK.value());
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\": true}");
                    });
                    logout.deleteCookies("JSESSIONID");
                    logout.invalidateHttpSession(true);
                    logout.permitAll();
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .invalidSessionUrl("/login.html")
                )
                .headers(headers -> headers
                        .cacheControl(cache -> cache.disable())
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api")) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\": \"Unauthorized\"}");
                            } else {
                                response.sendRedirect("/login.html");
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
                "http://localhost:8888",
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}