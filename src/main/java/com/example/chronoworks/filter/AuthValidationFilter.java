package com.example.chronoworks.filter;

import com.example.chronoworks.service.EmpleadoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthValidationFilter extends OncePerRequestFilter {

    private final EmpleadoService empleadoService;

    public AuthValidationFilter(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if(isProtectedPage(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isProtectedPage(request)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (!isAuthenticated(authentication)) {
                response.sendRedirect("/login.html");
                return;
            }

            // Verificar si el usuario está activo
            if (authentication != null && !isUserActive(authentication)) {
                SecurityContextHolder.clearContext();
                response.sendRedirect("/login.html?error=account_inactive");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/") ||
                uri.equals("/login.html") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/img/") ||
                uri.equals("/favicon.ico") ||
                uri.startsWith("/api/public/") ||
                uri.startsWith("/api/auth/login");
    }

    private boolean isProtectedPage(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/admin") ||
                request.getRequestURI().startsWith("/lider") ||
                request.getRequestURI().startsWith("/agente");
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean isUserActive(Authentication authentication) {
        return empleadoService.findByUsuario(authentication.getName())
                .map(empleado -> empleado.isActivo())
                .orElse(false);
    }
}