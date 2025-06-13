package com.example.chronoworks.repository;

import com.example.chronoworks.model.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    Optional<Empresa> findByNombreEmpresa(String nombreEmpresa);

    Page<Empresa> findByActivoTrue(Pageable pageable);
}
