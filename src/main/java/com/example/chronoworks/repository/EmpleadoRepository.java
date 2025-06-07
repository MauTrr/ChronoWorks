package com.example.chronoworks.repository;

import com.example.chronoworks.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    Optional<Empleado> findByCorreo(String correo);
}
