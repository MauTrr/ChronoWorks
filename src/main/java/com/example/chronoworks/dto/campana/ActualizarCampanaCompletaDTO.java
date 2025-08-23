package com.example.chronoworks.dto.campana;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ActualizarCampanaCompletaDTO {
    @Valid
    @NotNull(message = "Datos de campaña son requeridos")
    private CampanaDTO campana;

    @NotNull(message = "Asignaciones son requeridas")
    private List<@Valid AsignacionCampanaDTO> asignaciones;
}
