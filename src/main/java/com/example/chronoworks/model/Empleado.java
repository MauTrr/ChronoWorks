package com.example.chronoworks.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name= "empleados")
@Data
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @Column(name = "nombre", nullable = false)
    private String nombre;
    @Column(name = "apellido", nullable = false)
    private String apellido;
    @Column(name = "correo", unique = true, nullable = false)
    private String correo;
    @Column(name = "telefono", nullable = false)
    @Pattern(regexp = "^\\d{10}$")
    private String telefono;
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_turno", nullable = false)
    private Turno turno;

    @OneToOne(mappedBy = "empleado", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private Credencial credencial;

    private boolean activo = true;
}
