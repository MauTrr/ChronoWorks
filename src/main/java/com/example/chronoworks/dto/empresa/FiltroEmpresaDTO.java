package com.example.chronoworks.dto.empresa;

import lombok.Data;

@Data
public class FiltroEmpresaDTO {
    private String nombreEmpresa;
    private String sector;
    private Boolean activo;
}
