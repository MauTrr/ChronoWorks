package com.example.chronoworks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaController {

    // Muestra la vista principal del Admin
    @GetMapping("/admin")
    public String mostrarVistaAdmin() {
        return "admin";
    }

    // Muestra la vista principal del Líder
    @GetMapping("/lider")
    public String mostrarVistaLider() {
        return "lider";
    }

    // Muestra la vista principal del Agente
    @GetMapping("/agente")
    public String mostrarVistaAgente() {
        return "agente";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accesoDenegado() {
        return "access-denied";
    }
}
