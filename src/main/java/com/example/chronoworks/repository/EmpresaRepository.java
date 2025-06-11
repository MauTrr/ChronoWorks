package com.example.chronoworks.repository;

import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    Optional<Empleado> findByNombreEmpresa(String nombreEmpresa);

    List<Empresa> findByNombreEmpresaContainingIgnoreCase(String nombreEmpresa);
}
