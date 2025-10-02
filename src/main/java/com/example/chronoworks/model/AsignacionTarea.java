package com.example.chronoworks.model;

import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asignacion_tarea")
@Data
public class AsignacionTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Integer idAsignacion;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @OneToMany(mappedBy = "asignacionTarea", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AsignacionEmpleadoTarea> asignacionesEmpleados = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea", nullable = false)
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campaña", nullable = false)
    private Campana campana;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private AsignacionCampanaEstado estado = AsignacionCampanaEstado.ACTIVA;
}