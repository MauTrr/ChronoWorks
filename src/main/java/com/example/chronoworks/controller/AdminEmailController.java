package com.example.chronoworks.controller;

import com.example.chronoworks.dto.email.EmailRequestDTO;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.repository.EmpleadoRepository;
import com.example.chronoworks.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/email")
public class AdminEmailController {

    private final EmailService emailService;
    private final EmpleadoRepository empleadoRepository;

    public AdminEmailController(EmailService emailService,
                                EmpleadoRepository empleadoRepository) {
        this.emailService = emailService;
        this.empleadoRepository = empleadoRepository;
    }

    @PostMapping("/send-massive")
    public ResponseEntity<String> sendMassive(@RequestBody EmailRequestDTO dto) {

        List<String> destinatarios = emailService.obtenerCorreosPorRoles(dto.getRoles());

        emailService.sendMassiveEmail(destinatarios, dto.getSubject(), dto.getBody());

        return ResponseEntity.ok("Envío masivo iniciado a " + destinatarios.size() + " empleados.");
    }
}


