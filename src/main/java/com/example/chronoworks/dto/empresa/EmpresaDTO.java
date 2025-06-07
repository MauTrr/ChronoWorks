package com.example.chronoworks.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaDTO {

    @NotBlank(message = "El nombre de la empresa no puede estar vacio")
    @Size(message = "El nombre de la empresa excede 50 caracteres")
    private String nombreEmpresa;

    @Size(message = "La direccion de la empresa excede 25 caracteres")
    private String direccion;

    @Size(message = "El telefono de la empresa excede 15 caracteres")
    private String telefono;

    @Size(message = "El sector excede 25 caracteres")
    private String sector;

    @Size(message = "El nombre del encargado de la empresa excede 50 caracteres")
    private String encargado;
}
