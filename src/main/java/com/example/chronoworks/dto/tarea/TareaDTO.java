package com.example.chronoworks.dto.tarea;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TareaDTO {
    @NotBlank(message = "El nombre de la tarea no puede estar vacio")
    private String nombreTarea;

    private String detalles;

    private boolean activo;
}
