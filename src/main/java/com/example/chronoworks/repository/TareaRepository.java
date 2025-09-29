package com.example.chronoworks.repository;

import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Tarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Integer> {

    Optional<Tarea> findByNombreTarea(String nombreTarea);

    //Page<Tarea> findByActivoTrue(Pageable pageable);
}
