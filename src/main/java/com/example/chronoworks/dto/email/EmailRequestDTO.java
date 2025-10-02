package com.example.chronoworks.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class EmailRequestDTO {

    // Opción 1: correos directos
    private List<@Email String> destinatarios;

    // Opción 2: roles para buscarlos en BD
    private List<String> roles;

    @NotBlank(message = "El asunto no puede estar vacío")
    private String subject;

    @NotBlank(message = "El cuerpo del mensaje no puede estar vacío")
    private String body;
}
