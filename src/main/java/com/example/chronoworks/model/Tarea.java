package com.example.chronoworks.model;

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

    @Column(name = "detalles")
    private String detalles;

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private  java.util.List<Asignacion> asignaciones;

    private boolean activo = true;
}
