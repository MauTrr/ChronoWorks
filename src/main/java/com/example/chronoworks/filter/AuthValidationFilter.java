/*package com.example.chronoworks.filter;

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
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // permitir recursos estáticos y endpoints públicos (evitar loop)
        if (isPublicPage(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // solo validar rutas protegidas (admin/lider/agente u otras que quiera proteger)
        if (isProtectedPage(uri)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (!isAuthenticated(authentication)) {
                // redirigir a login.html y retornar (no seguir ejecutando)
                response.sendRedirect("/login.html");
                return;
            }

            // comprobar si usuario activo (si aplica)
            if (authentication != null && !isUserActive(authentication)) {
                SecurityContextHolder.clearContext();
                response.sendRedirect("/login.html?error=account_inactive");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPage(String uri) {
        return uri.equals("/") ||
                uri.equals("/index.html") ||
                uri.equals("/login.html") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/img/") ||
                uri.startsWith("/static/") ||
                uri.equals("/favicon.ico") ||
                uri.startsWith("/api/public/") ||
                uri.startsWith("/api/auth/") ||
                uri.startsWith("/error") ||
                uri.equals("/health") ||
                uri.startsWith("/public/");
    }

    private boolean isProtectedPage(String uri) {
        return uri.startsWith("/admin") ||
                uri.startsWith("/lider") ||
                uri.startsWith("/agente") ||
                uri.startsWith("/api/Admin/") ||
                uri.startsWith("/api/lider/") ||
                uri.startsWith("/api/agente/");
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean isUserActive(Authentication authentication) {
        // ajustar según tu servicio: buscar por username o id
        return empleadoService.findByUsuario(authentication.getName())
                .map(empleado -> empleado.isActivo())
                .orElse(false);
    }
}*/