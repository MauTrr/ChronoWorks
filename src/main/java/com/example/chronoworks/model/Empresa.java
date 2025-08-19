package com.example.chronoworks.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "empresa")
@Data
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEmpresa;

    private String nombreEmpresa;
    private String nitEmpresa;
    private String direccion;
    private String telefono;
    private String sector;
    private String representante;
    private boolean activo = true;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<Campana> campanas;
}
