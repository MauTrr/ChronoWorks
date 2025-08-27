package com.example.chronoworks.model;

import com.example.chronoworks.model.enums.TareaTipos;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tarea")
@Data
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarea")
    private Integer idTarea;

    @Column(name = "nombre_tarea")
    private String nombreTarea;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TareaTipos tipo;

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private  java.util.List<AsignacionTarea> asignaciones;
}
