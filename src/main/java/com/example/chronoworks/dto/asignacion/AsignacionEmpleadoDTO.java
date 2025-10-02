package com.example.chronoworks.dto.asignacion;

import com.example.chronoworks.model.enums.AsignacionTareaEstado;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data

public class AsignacionEmpleadoDTO {
    @NotNull(message = "El ID del empleado no puede ser nulo")
    private Integer idEmpleado;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    @NotNull(message = "El estado del empleado en la asignación es obligatorio")
    private AsignacionTareaEstado estado;
}
