package com.example.chronoworks.controller;

import com.example.chronoworks.dto.login.RequestLoginDTO;
import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.security.CustomUserDetailsService;
import com.example.chronoworks.service.EmpleadoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmpleadoService empleadoService;

    public LoginController(AuthenticationManager authenticationManager,
                           CustomUserDetailsService userDetailsService,
                           EmpleadoService empleadoService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.empleadoService = empleadoService;

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RequestLoginDTO request,
                                   HttpServletRequest httpRequest) {
        try {
            Empleado empleado = empleadoService.findByUsuario(request.getUsuario())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales Invalidas"));
            if (empleado != null && !empleado.isActivo()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Cuenta desactivada")
                );
            }

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

            Credencial credencial = empleado.getCredencial();

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


    @GetMapping("/validate")
    public ResponseEntity<?> validateSession(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verificar si el empleado está activo
        String username = authentication.getName();
        Empleado empleado = empleadoService.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (empleado == null || !empleado.isActivo()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();


    }

    @GetMapping("/current-role")
    public ResponseEntity<?> getCurrentRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        String username = authentication.getName();
        Empleado empleado = empleadoService.findByUsuario(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales Invalidas"));

        // Verificar estado del empleado
        if (empleado == null || !empleado.isActivo()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok().body(Map.of(
                "role", role,
                "normalizedRole", role.replace("ROLE_", ""),
                "isAdmin", role.equals("ROLE_ADMIN"),
                "isLider", role.equals("ROLE_LIDER"),
                "isAgente", role.equals("ROLE_AGENTE"),
                "isActive", empleado.isActivo()
        ));
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