package com.example.chronoworks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaController {

    // Muestra la vista principal del Admin
    @GetMapping("/admin.html")
    public String mostrarVistaAdmin() {
        return "admin"; // Llama a templates/admin.html o static/admin.html
    }

    // Muestra la vista principal del Líder
    @GetMapping("/lider.html")
    public String mostrarVistaLider() {
        return "lider"; // Llama a templates/lider.html o static/lider.html
    }

    // Muestra la vista principal del Agente
    @GetMapping("/agente.html")
    public String mostrarVistaAgente() {
        return "agente"; // Llama a templates/agente.html o static/agente.html
    }

    // Página de login personalizada
    @GetMapping("/login")
    public String login() {
        return "login"; // login.html debe estar en static/ o templates/
    }

    // Página de acceso denegado (opcional)
    @GetMapping("/access-denied")
    public String accesoDenegado() {
        return "access-denied"; // Puedes crear access-denied.html
    }
}

