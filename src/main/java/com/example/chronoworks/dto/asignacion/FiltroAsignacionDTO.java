package com.example.chronoworks.dto.asignacion;

import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FiltroAsignacionDTO {
    private String nombreEmpleado;
    private String apellidoEmpleado;
    private String nombreCampana;
    private LocalDateTime fechaAsignacion;
    private AsignacionCampanaEstado estado;
}
