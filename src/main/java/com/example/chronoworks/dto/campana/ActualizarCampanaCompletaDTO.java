package com.example.chronoworks.dto.campana;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class ActualizarCampanaCompletaDTO {
    @Valid
    private CampanaDTO campana;

    private List<@Valid AsignacionCampanaDTO> asignaciones;
}
