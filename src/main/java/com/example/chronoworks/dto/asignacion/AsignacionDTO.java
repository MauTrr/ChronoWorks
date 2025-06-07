package com.example.chronoworks.dto.asignacion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AsignacionDTO {
    @NotNull(message = "La fecha de asignacion no puede ser nula")
    @PastOrPresent(message = "La fecha de asignacion no puede ser futura")
    private LocalDate fecha;

    private String observaciones;

    @NotNull(message = "El ID de la tarea no puede ser nulo")
    private Integer idTarea;

    @NotNull(message = "El ID de la campaña no puede ser nulo")
    private Integer idCampaña;

    @NotNull(message = "El ID del empleado no puede ser nulo")
    private Integer idEmpleado;
}
