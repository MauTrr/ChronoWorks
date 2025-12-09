package com.example.chronoworks.controller;

import com.example.chronoworks.dto.ClienteForm;
import com.example.chronoworks.service.ClienteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping("/admin/clientes/add")
    public String agregarCliente(ClienteForm clienteForm) {
        clienteService.guardarCliente(clienteForm);
        return "redirect:/admin?cliente_agregado=true";
    }
}

