package com.example.chronoworks.repository;

import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredencialRepository extends JpaRepository<Credencial, Integer> {

    Optional<Credencial> findByEmpleadoIdEmpleado(Integer idEmpleado);
    Optional<Credencial> findByEmpleado(Empleado empleado);
    Optional<Credencial> findByUsuario(String usuario);
}
