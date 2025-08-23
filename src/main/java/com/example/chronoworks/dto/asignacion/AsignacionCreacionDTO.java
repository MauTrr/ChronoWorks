package com.example.chronoworks.dto.asignacion;

import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsignacionCreacionDTO {
    @NotNull(message = "La fecha de asignacion no puede ser nula")
    @PastOrPresent(message = "La fecha de asignacion no puede ser futura")
    private LocalDateTime fecha;

    private String observaciones;

    @NotNull(message = "El estado inicial de la asignacion es obligatorio")
    private AsignacionCampanaEstado estado;

    @NotNull(message = "El ID de la tarea no puede ser nulo")
    private Integer idTarea;

    @NotNull(message = "El ID de la campaña no puede ser nulo")
    private Integer idCampana;

    @NotNull(message = "El ID del empleado no puede ser nulo")
    private Integer idEmpleado;
}
