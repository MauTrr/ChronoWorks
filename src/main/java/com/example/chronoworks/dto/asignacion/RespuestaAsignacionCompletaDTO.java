package com.example.chronoworks.dto.asignacion;

import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RespuestaAsignacionCompletaDTO {
    private Integer idAsignacion;
    private LocalDateTime fecha;
    private AsignacionCampanaEstado estado;
    private Integer idTarea;
    private String nombreTarea;
    private String descripcionTarea;
    private Integer idCampana;
    private String nombreCampana;
    private List<RespuestaAsignacionEmpleadoDTO> empleados;
}
