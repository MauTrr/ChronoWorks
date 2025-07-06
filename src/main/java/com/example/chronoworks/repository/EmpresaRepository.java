package com.example.chronoworks.repository;

import com.example.chronoworks.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer>, JpaSpecificationExecutor<Empresa> {
    Optional<Empresa> findByNombreEmpresa(String nombreEmpresa);
}
