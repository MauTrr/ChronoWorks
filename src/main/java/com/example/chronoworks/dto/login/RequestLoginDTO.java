package com.example.chronoworks.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestLoginDTO {
    @NotBlank(message = "El usuario es olbigatorio")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}
