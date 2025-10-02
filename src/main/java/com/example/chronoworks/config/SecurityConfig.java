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
                .authorizeHttpRequests(auth -> auth
                        // ✅ Archivos estáticos (permitir TODO lo de /static)
                        .requestMatchers(
                                "/",
                                "/index.html",
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
                .formLogin(form -> form.disable()) // deshabilitar login de Spring
                .logout(logout -> {
                    logout.logoutUrl("/api/auth/logout");
                    logout.logoutSuccessHandler((request, response, authentication) -> {
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\": true}");
                    });
                    logout.deleteCookies("JSESSIONID");
                    logout.invalidateHttpSession(true);
                    logout.permitAll();
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .invalidSessionUrl("/login.html")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login.html?session=expired")
                )
                .headers(headers -> headers
                        .cacheControl(cache -> cache.disable())
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                                .preload(true)
                        )
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api")) {
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                            } else {
                                response.sendRedirect("/login.html");
                            }
                        })
                )
                .addFilterBefore(new AuthValidationFilter(empleadoService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
