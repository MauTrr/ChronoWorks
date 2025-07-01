package com.example.chronoworks.controller;

import com.example.chronoworks.dto.login.RequestLoginDTO;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.security.CustomUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public LoginController(AuthenticationManager authenticationManager,
                           CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService  = userDetailsService;
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
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsuario());
            return ResponseEntity.ok(userDetails);
        } catch (BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales invalidas");
        }
    }
}
