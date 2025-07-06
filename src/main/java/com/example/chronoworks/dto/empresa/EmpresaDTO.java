package com.example.chronoworks.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaDTO {
    @NotBlank(message = "El nombre de la empresa no puede estar vacio")
    @Size(max = 50)
    private String nombreEmpresa;

    @NotBlank(message = "El NIT de la empresa no puede estar vacio")
    @Size(max = 25)
    private String nitEmpresa;

    @Size(max = 50)
    private String direccion;

    @Size(max = 15)
    private String telefono;

    @Size(max = 25)
    private String sector;

    @Size(max = 50)
    private String lider;

    private Boolean activo = true; // por defecto activa

}