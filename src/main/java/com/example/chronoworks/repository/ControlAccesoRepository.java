package com.example.chronoworks.repository;

import com.example.chronoworks.model.ControlAcceso;
import com.example.chronoworks.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControlAccesoRepository extends JpaRepository<ControlAcceso, Integer> {

    //Metodo para encontrar el ultimo registro de ingreso de un empleado que aun no ha marcado su salida
    Optional<ControlAcceso> findFirstByEmpleadoAndHoraSalidaIsNullOrderByHoraEntradaDesc(Empleado empleado);

    //Metodo para obtener el historial del control de acceso de un empleaddo
    Optional<ControlAcceso> findByEmpleadoOrderByHoraEntradaDesc(Empleado empleado);
}
