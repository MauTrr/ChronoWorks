package com.example.chronoworks.service;

import com.example.chronoworks.model.AsignacionEmpleadoTarea;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.enums.AsignacionTareaEstado;
import com.example.chronoworks.repository.AsignacionEmpleadoTareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaEmpleadoService {

    private final AsignacionEmpleadoTareaRepository asignacionEmpleadoTareaRepository;

    // Obtener todos los empleados asignados a una tarea
    public List<Empleado> obtenerEmpleadosPorTarea(Integer idTarea) {
        return asignacionEmpleadoTareaRepository.findEmpleadosActivosByTarea(idTarea);
    }

    // Obtener empleados con estados específicos
    public List<AsignacionEmpleadoTarea> obtenerAsignacionesPorTarea(Integer idTarea) {
        List<AsignacionTareaEstado> estadosActivos = Arrays.asList(
                AsignacionTareaEstado.ASIGNADA,
                AsignacionTareaEstado.EN_PROCESO
        );

        return asignacionEmpleadoTareaRepository.findEmpleadosByTareaAndEstado(
                idTarea, estadosActivos
        );
    }

    // Verificar si un empleado está asignado a una tarea
    public boolean estaEmpleadoAsignadoATarea(Integer idTarea, Integer idEmpleado) {
        return asignacionEmpleadoTareaRepository.existsAsignacionVigente(idTarea, idEmpleado);
    }
}