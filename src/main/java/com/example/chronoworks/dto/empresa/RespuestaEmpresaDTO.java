package com.example.chronoworks.dto.empresa;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RespuestaEmpresaDTO {
    private Integer idEmpresa;
    private String nombreEmpresa;
    private String  direccion;
    private String telefono;
    private String sector;
    private String encargado;
}
