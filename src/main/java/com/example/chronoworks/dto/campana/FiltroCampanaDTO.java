package com.example.chronoworks.dto.campana;

import com.example.chronoworks.model.enums.CampanaEstado;
import lombok.Data;

import java.util.List;

@Data
public class FiltroCampanaDTO {
    private String nombreCampana;
    private String nombreEmpresa;
    private List<CampanaEstado> estados;
    private Boolean excluirArchivadas;
}
