package com.example.chronoworks.filter;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        if (isProtectedPage(request) && !isAuthenticated(request)) {
            response.sendRedirect("/login.html");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPage(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/admin") ||
                request.getRequestURI().startsWith("/lider") ||
                request.getRequestURI().startsWith("/agente");
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return  authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
