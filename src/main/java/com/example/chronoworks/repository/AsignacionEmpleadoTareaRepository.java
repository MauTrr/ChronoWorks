package com.example.chronoworks.repository;

import com.example.chronoworks.model.AsignacionEmpleadoTarea;
import com.example.chronoworks.model.AsignacionTarea;
import com.example.chronoworks.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionEmpleadoTareaRepository extends JpaRepository<AsignacionEmpleadoTarea, Integer>, JpaSpecificationExecutor<AsignacionEmpleadoTarea> {

    List<AsignacionEmpleadoTarea> findByAsignacionTarea(AsignacionTarea asignacionTarea);

    boolean existsByEmpleadoAndAsignacionTarea(Empleado empleado, AsignacionTarea asignacionTarea);

    Optional<AsignacionEmpleadoTarea> findByAsignacionTareaIdAsignacionAndEmpleadoIdEmpleado(Integer idAsignacion, Integer idEmpleado);
}
