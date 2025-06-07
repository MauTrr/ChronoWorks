package com.example.chronoworks.service;


import com.example.chronoworks.dto.controlacceso.RespuestaRegistroDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.ControlAcceso;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.repository.ControlAccesoRepository;
import com.example.chronoworks.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ControlAccesoService {

    private ControlAccesoRepository controlAccesoRepository;
    private EmpleadoRepository empleadoRepository;


    public ControlAccesoService(ControlAccesoRepository controlAccesoRepository, EmpleadoRepository empleadoRepository) {
        this.controlAccesoRepository = controlAccesoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional
    public RespuestaRegistroDTO registrarEntrada(RespuestaRegistroDTO  dto) {
        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado con la ID " + dto.getIdEmpleado() +  " no encontrado"));
        Optional<ControlAcceso> registroAbierto = controlAccesoRepository.findUltimoEmpleadoActivo(empleado);
        if (registroAbierto.isPresent()) {
            throw new BadRequestException("El empleado con ID " + dto.getIdEmpleado() + " ya tiene un registro abierto. Debe registrar primero su salida.");
        }

        ControlAcceso nuevoRegistro = new ControlAcceso();
        nuevoRegistro.setEmpleado(empleado);
        nuevoRegistro.setHoraSalida(null);
        nuevoRegistro.setObservacionEntrada(dto.getObservacionEntrada());

        ControlAcceso registroGuardado = controlAccesoRepository.save(nuevoRegistro);
        return mapToControlAccesoRespuestaDTO(registroGuardado);
    }

    private RespuestaRegistroDTO mapToControlAccesoRespuestaDTO(ControlAcceso controlAcceso) {
        String nombreCompletoEmpleado = null;
        if (controlAcceso.getEmpleado() != null) {
            nombreCompletoEmpleado = controlAcceso.getEmpleado().getNombre() + " " + controlAcceso.getEmpleado().getApellido();
        }
        return RespuestaRegistroDTO.builder()
                .idControl(controlAcceso.getIdControl())
                .idEmpleado(controlAcceso.getEmpleado() != null ? controlAcceso.getEmpleado().getIdEmpleado().intValue() : null)
                .nombre(nombreCompletoEmpleado)
                .horaEntrada(controlAcceso.getHoraEntrada())
                .horaSalida(controlAcceso.getHoraSalida())
                .observacionEntrada(controlAcceso.getObservacionEntrada())
                .observacionSalida(controlAcceso.getObservacionSalida())
                .build();
    }


}
