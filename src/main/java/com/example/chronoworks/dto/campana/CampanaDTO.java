package com.example.chronoworks.dto.campana;

import com.example.chronoworks.model.enums.CampanaEstado;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CampanaDTO {
    private Integer idCampana;

    @NotBlank(message = "El nombre de la campaña no puede estar vacio")
    private String nombreCampana;

    private String descripcion;

    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin de la campaña no puede ser nula")
    @FutureOrPresent(message = "La fecha de fin de la campaña no puede ser pasada")
    private LocalDate fechaFin;

    @NotNull(message = "El ID de la empresa no puede ser nulo")
    private Integer idEmpresa;


    private CampanaEstado estado;

    private Integer idLider;
    private List<Integer> idsAgentes;

}
