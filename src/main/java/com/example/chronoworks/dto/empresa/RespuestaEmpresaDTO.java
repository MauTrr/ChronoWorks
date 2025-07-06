package com.example.chronoworks.dto.empresa;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RespuestaEmpresaDTO {
    private Integer id;
    private String nombreEmpresa;
    private String nitEmpresa;
    private String direccion;
    private String telefono;
    private String sector;
    private String lider;
    private boolean activo;
}

