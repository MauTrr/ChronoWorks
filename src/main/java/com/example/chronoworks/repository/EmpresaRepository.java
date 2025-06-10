package com.example.chronoworks.repository;

import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    Optional<Empleado> findByNombreEmpresa(String nombreEmpresa);

    List<Empresa> findByNombreEmpresaContainingIgnoreCase(String nombreEmpresa);
}
