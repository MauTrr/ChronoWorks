package com.example.chronoworks.controller;

import com.example.chronoworks.dto.login.RequestLoginDTO;
import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.security.CustomUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> login(@RequestBody RequestLoginDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsuario(),
                            request.getContrasena()
                    )
            );

            // Obtener datos completos del usuario
            Credencial credencial = userDetailsService.getCredencialByUsuario(request.getUsuario());

            return ResponseEntity.ok().body(
                    new Object() {
                        public final String usuario = credencial.getUsuario();
                        public final String rol = credencial.getRol().getNombreRol();
                        public final Integer idEmpleado = credencial.getEmpleado().getIdEmpleado();
                    }
            );

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }
}