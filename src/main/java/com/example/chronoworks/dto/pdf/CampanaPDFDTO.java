package com.example.chronoworks.dto.pdf;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CampanaPDFDTO {
    private Integer idCampana;
    private String nombreCampana;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String nombreEmpresa;
    private String estado;
    private String lider;
    private Integer cantidadAgentes;
    private String agentes;
}
