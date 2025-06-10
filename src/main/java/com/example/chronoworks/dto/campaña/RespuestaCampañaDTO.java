package com.example.chronoworks.dto.campaña;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RespuestaCampañaDTO {
    private Integer idCampaña;
    private String nombreCampaña;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer idEmpresa;
    private String nombreEmpresa;
}
