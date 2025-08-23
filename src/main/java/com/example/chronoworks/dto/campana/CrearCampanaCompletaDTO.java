package com.example.chronoworks.dto.campana;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CrearCampanaCompletaDTO {
    @Valid
    private CampanaDTO campana;

    @NotEmpty(message = "Debe haber al menos una asignación")
    private List<@Valid AsignacionCampanaDTO> asignaciones;
}
