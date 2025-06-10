package com.example.chronoworks.dto.asignacion;

import com.example.chronoworks.model.enums.AsignacionEstado;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RespuestaAsignacionDTO {
    private Integer idAsignacion;
    private LocalDateTime fecha;
    private String observaciones;
    private Integer idTarea;
    private String nombreTarea;
    private String detallesTarea;
    private Integer idCampaña;
    private String nombreCampaña;
    private String descripcionCampaña;
    private Integer idEmpleado;
    private String nombre;
    private String apellido;
    private AsignacionEstado estado;
}
