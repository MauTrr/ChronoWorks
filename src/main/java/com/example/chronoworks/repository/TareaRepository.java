package com.example.chronoworks.repository;

import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TareaRepository extends JpaRepository<Tarea, Integer> {

    Optional<Tarea> findByNombreTarea(String nombreTarea);
}
