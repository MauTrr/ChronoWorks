package com.example.chronoworks.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaController {

    @GetMapping("/admin")
    public String admin(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/admin.html";  // ✅ Sin subcarpeta
        }
        return "redirect:/login.html";
    }

    @GetMapping("/lider")
    public String lider(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/lider.html";  // ✅ Sin subcarpeta
        }
        return "redirect:/login.html";
    }

    @GetMapping("/agente")
    public String agente(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/agente.html";  // ✅ Sin subcarpeta
        }
        return "redirect:/login.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/access-denied")
    public String accesoDenegado() {
        return "access-denied";
    }
}
