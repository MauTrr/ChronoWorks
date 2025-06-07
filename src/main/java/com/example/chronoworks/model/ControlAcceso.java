package com.example.chronoworks.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "control_acceso")
@Data
public class ControlAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_control")
    private Integer idControl;

    @Column(name = "fecha")
    private LocalDate fecha;
    @Column(name = "hora_entrada", nullable = false, updatable = false)
    private LocalTime horaEntrada;
    @Column(name = "hora_salida")
    private LocalTime horaSalida;
    @Column(name = "observacion_entrada")
    private String observacionEntrada;
    @Column(name = "observacion_salida")
    private String observacionSalida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;
}
