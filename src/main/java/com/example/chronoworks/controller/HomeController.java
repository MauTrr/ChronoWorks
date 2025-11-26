package com.example.chronoworks.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Redirige según el rol del usuario
            return "redirect:/dashboard.html";
        }
        // Si no está autenticado, muestra la página de inicio pública
        return "index";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard.html")
    public String dashboard(Authentication authentication) {
        if (authentication != null) {
            // Lógica para determinar a qué dashboard redirigir según el rol
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin.html";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_LIDER"))) {
                return "redirect:/lider.html";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENTE"))) {
                return "redirect:/agente.html";
            }
        }
        return "redirect:/login.html";
    }
}