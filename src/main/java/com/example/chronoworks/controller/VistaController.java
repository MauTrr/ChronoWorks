package com.example.chronoworks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaController {

    // Muestra la vista principal del Admin
    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin/admin.html";
    }

    // Muestra la vista principal del Líder
    @GetMapping("/lider")
    public String lider() {
        return "forward:/lider/lider.html";
    }

    // Muestra la vista principal del Agente
    @GetMapping("/agente")
    public String agente() {
        return "forward:/agente/agente.html";
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
