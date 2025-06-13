package com.example.chronoworks.repository;

import com.example.chronoworks.model.ControlAcceso;
import com.example.chronoworks.model.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ControlAccesoRepository extends JpaRepository<ControlAcceso, Integer> {

    boolean existsByEmpleadoAndHoraSalidaIsNull(Empleado empleado);

    //Metodo para encontrar el ultimo registro de ingreso de un empleado que aun no ha marcado su salida
    Optional<ControlAcceso> findFirstByEmpleadoAndHoraSalidaIsNullOrderByHoraEntradaDesc(Empleado empleado);

    Page<ControlAcceso> findByEmpleadoOrderByHoraEntradaDesc(Empleado empleado, Pageable pageable);

}
