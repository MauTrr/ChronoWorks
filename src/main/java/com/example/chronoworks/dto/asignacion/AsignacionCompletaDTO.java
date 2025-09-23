package com.example.chronoworks.dto.asignacion;

import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AsignacionCompletaDTO {
    @NotNull(message = "La fecha de asignacion no puede ser nula")
    private LocalDateTime fecha;

    @NotNull(message = "El estado inicial de la asignacion es obligatorio")
    private AsignacionCampanaEstado estado;

    @NotNull(message = "El ID de la tarea no puede ser nulo")
    private Integer idTarea;

    @NotNull(message = "El ID de la campaña no puede ser nulo")
    private Integer idCampana;

    @NotEmpty(message = "Debe asignar al menos un empleado")
    private List<AsignacionEmpleadoDTO> empleados;
}
