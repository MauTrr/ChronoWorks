package com.example.chronoworks.controller;

import com.example.chronoworks.dto.email.EmailRequestDTO;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.repository.EmpleadoRepository;
import com.example.chronoworks.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Enviar correos masivos según roles
     */
    @PostMapping("/send-massive")
    public ResponseEntity<String> sendMassiveEmail(@RequestBody EmailRequestDTO emailRequestDTO) {
        // 1. Buscar empleados por roles
        List<Empleado> empleados = empleadoRepository.findByNombreRolIn(emailRequestDTO.getRoles());

        // 2. Extraer correos
        List<String> destinatarios = empleados.stream()
                .map(Empleado::getCorreo)
                .collect(Collectors.toList());

        // 3. Enviar correos
        emailService.sendMassiveEmail(destinatarios,
                emailRequestDTO.getSubject(),
                emailRequestDTO.getBody());

        return ResponseEntity.ok("Correos enviados exitosamente a " + destinatarios.size() + " empleados.");
    }
}
