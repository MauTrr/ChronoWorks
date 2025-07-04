package com.example.chronoworks.controller;

import com.example.chronoworks.dto.login.RequestLoginDTO;
import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.IContext;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public LoginController(AuthenticationManager authenticationManager,
                           CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RequestLoginDTO request,
                                   HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsuario(),
                            request.getContrasena()
                    )
            );

            // Establecer autenticación en el contexto de seguridad
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authentication);

            // Crear sesión si no existe
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            Credencial credencial = userDetailsService.getCredencialByUsuario(request.getUsuario());

            return ResponseEntity.ok().body(
                    Map.of(
                            "success", true,
                            "rol", credencial.getRol().getNombreRol(),
                            "redirectUrl", getRedirectUrl(authentication),
                            "message", "Autenticación exitosa"
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("success", false, "message", "Credenciales inválidas")
            );
        }
    }

    private String getRedirectUrl(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return switch (role) {
            case "ROLE_ADMIN" -> "/admin";
            case "ROLE_LIDER" -> "/lider";
            case "ROLE_AGENTE" -> "/agente";
            default -> "/";
        };
    }
}