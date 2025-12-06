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

        List<Empleado> empleados = empleadoRepository.findByNombreRolIn(emailRequestDTO.getRoles());

        List<String> destinatarios = empleados.stream()
                .map(Empleado::getCorreo)
                .collect(Collectors.toList());

        //  ESTA ES LA LLAVE PARA ACTIVAR EL @ASYNC CORRECTO
        emailService.iniciarEnvio(destinatarios,
                emailRequestDTO.getSubject(),
                emailRequestDTO.getBody());

        return ResponseEntity.ok("Se inició el envío masivo a " + destinatarios.size() + " empleados.");
    }
}

