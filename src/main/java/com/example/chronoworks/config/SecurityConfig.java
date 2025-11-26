package com.example.chronoworks.config;

import com.example.chronoworks.filter.AuthValidationFilter;
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
                        // Archivos estáticos y páginas públicas
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/static/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Endpoints públicos
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Páginas restringidas por rol
                        .requestMatchers("/admin.html", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/agente.html", "/agente/**").hasRole("AGENTE")
                        .requestMatchers("/lider.html", "/lider/**").hasRole("LIDER")

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
                    logout.logoutSuccessHandler((request, response, authentication) -> {
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
                        .sessionFixation().none()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestUri = request.getRequestURI();

                            if (requestUri.startsWith("/api")) {
                                // APIs devuelven 401 en vez de redirigir
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\": \"Unauthorized\"}");
                            } else {
                                // Páginas web: redirigir al login SOLO si no estás ya en login.html
                                if (!requestUri.equals("/login.html")) {
                                    response.sendRedirect("/login.html");
                                } else {
                                    response.setStatus(HttpStatus.OK.value());
                                }
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/login.html?error=access_denied");
                        })
                )
                .addFilterBefore(new AuthValidationFilter(empleadoService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "http://localhost:8888",
                    "https://chronoworks-production.up.railway.app"
            ));
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
