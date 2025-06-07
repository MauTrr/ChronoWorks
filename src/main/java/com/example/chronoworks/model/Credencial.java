package com.example.chronoworks.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "credenciales")
@Data
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_credencial")
    private Integer idCredencial;

    @Column(name = "usuario",  nullable = false, unique = true)
    private String usuario;
    @Column(name = "contraseña", nullable = false)
    private  String contraseña;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado",nullable = false, unique = true)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
}
