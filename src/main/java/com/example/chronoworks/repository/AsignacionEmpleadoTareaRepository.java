package com.example.chronoworks.repository;

import com.example.chronoworks.model.AsignacionEmpleadoTarea;
import com.example.chronoworks.model.AsignacionTarea;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.enums.AsignacionTareaEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionEmpleadoTareaRepository extends JpaRepository<AsignacionEmpleadoTarea, Integer>, JpaSpecificationExecutor<AsignacionEmpleadoTarea> {

    List<AsignacionEmpleadoTarea> findByAsignacionTarea(AsignacionTarea asignacionTarea);

    boolean existsByEmpleadoAndAsignacionTarea(Empleado empleado, AsignacionTarea asignacionTarea);

    Optional<AsignacionEmpleadoTarea> findByAsignacionTareaIdAsignacionAndEmpleadoIdEmpleado(Integer idAsignacion, Integer idEmpleado);

    List<AsignacionEmpleadoTarea> findByEmpleadoIdEmpleado(Integer idEmpleado);

    // Metodo 1: Encontrar empleados asignados a una tarea específica
    @Query("SELECT aet FROM AsignacionEmpleadoTarea aet " +
            "JOIN aet.asignacionTarea at " +
            "JOIN at.tarea t " +
            "WHERE t.idTarea = :idTarea " +
            "AND aet.estado IN :estados " +
            "AND aet.empleado.activo = true")
    List<AsignacionEmpleadoTarea> findEmpleadosByTareaAndEstado(
            @Param("idTarea") Integer idTarea,
            @Param("estados") List<AsignacionTareaEstado> estados
    );

    // Metodo 2: Solo los empleados (sin toda la información de asignación)
    @Query("SELECT aet.empleado FROM AsignacionEmpleadoTarea aet " +
            "JOIN aet.asignacionTarea at " +
            "JOIN at.tarea t " +
            "WHERE t.idTarea = :idTarea " +
            "AND aet.estado IN ('ASIGNADA', 'EN_PROCESO') " +
            "AND aet.empleado.activo = true")
    List<Empleado> findEmpleadosActivosByTarea(@Param("idTarea") Integer idTarea);

    // Metodo 3: Para verificar asignaciones vigentes de un empleado a una tarea
    @Query("SELECT aet FROM AsignacionEmpleadoTarea aet " +
            "WHERE aet.asignacionTarea.tarea.idTarea = :idTarea " +
            "AND aet.empleado.idEmpleado = :idEmpleado " +
            "AND aet.estado IN ('ASIGNADA', 'EN_PROCESO')")
    List<AsignacionEmpleadoTarea> findAsignacionVigente(
            @Param("idTarea") Integer idTarea,
            @Param("idEmpleado") Integer idEmpleado
    );

    // Metodo 4: Versión simplificada del anterior (solo verifica existencia)
    @Query("SELECT COUNT(aet) > 0 FROM AsignacionEmpleadoTarea aet " +
            "WHERE aet.asignacionTarea.tarea.idTarea = :idTarea " +
            "AND aet.empleado.idEmpleado = :idEmpleado " +
            "AND aet.estado IN ('ASIGNADA', 'EN_PROCESO') " +
            "AND aet.empleado.activo = true")
    boolean existsAsignacionVigente(
            @Param("idTarea") Integer idTarea,
            @Param("idEmpleado") Integer idEmpleado
    );
}
