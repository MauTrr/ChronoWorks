package com.example.chronoworks.dto.campana;

import com.example.chronoworks.model.enums.CampanaEstado;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoDTO {
    @NotNull
    private CampanaEstado estado;
    private boolean liberarEmpleados = false;
}
