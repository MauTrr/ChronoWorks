package com.example.chronoworks.dto.campana;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignacionCampanaDTO {
    @NotNull
    private Integer idEmpleado;

    @NotNull Boolean esLider;
}
