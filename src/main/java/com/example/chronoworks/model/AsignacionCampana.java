package com.example.chronoworks.model;

import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignacion_campana")
@Data
public class AsignacionCampana {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion_campana")
    private Integer idAsignacionCampana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`id_campaña`", nullable = false)
    private Campana campana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "es_lider", nullable = false)
    private Boolean esLider = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_asignacion", nullable = false)
    private AsignacionCampanaEstado estado = AsignacionCampanaEstado.ACTIVA;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_liberacion")
    private LocalDateTime fechaLiberacion;

    @Column(name = "observaciones")
    private String observaciones;
}
