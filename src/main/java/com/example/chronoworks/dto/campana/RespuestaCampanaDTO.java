package com.example.chronoworks.dto.campana;

import com.example.chronoworks.model.enums.CampanaEstado;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RespuestaCampanaDTO {
    private Integer idCampana;
    private String nombreCampana;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer idEmpresa;
    private String nombreEmpresa;
    private CampanaEstado estado;
    private Integer idLider;
    private String nombreLider;
    private List<Integer> idsAgentes;
    private String nombresAgentes;
}
