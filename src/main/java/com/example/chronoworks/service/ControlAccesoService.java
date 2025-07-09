package com.example.chronoworks.service;


import com.example.chronoworks.dto.controlacceso.RegistroEntradaDTO;
import com.example.chronoworks.dto.controlacceso.RegistroSalidaDTO;
import com.example.chronoworks.dto.controlacceso.RespuestaRegistroDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.ControlAcceso;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.repository.ControlAccesoRepository;
import com.example.chronoworks.repository.EmpleadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;


@Service
public class ControlAccesoService {

    private final ControlAccesoRepository controlAccesoRepository;
    private final EmpleadoRepository empleadoRepository;


    public ControlAccesoService(ControlAccesoRepository controlAccesoRepository, EmpleadoRepository empleadoRepository) {
        this.controlAccesoRepository = controlAccesoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional
    public RespuestaRegistroDTO registrarEntrada(RegistroEntradaDTO dto) {
        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado."));

        if(controlAccesoRepository.existsByEmpleadoAndHoraSalidaIsNull(empleado)){
            throw new BadRequestException("El empleado ya tiene un registro abierto");
        }

        ControlAcceso nuevoRegistro = new ControlAcceso();
        nuevoRegistro.setEmpleado(empleado);
        nuevoRegistro.setFecha(LocalDate.now());
        nuevoRegistro.setHoraEntrada(LocalTime.now());
        nuevoRegistro.setObservacionEntrada(dto.getObservacionEntrada());

        ControlAcceso registroGuardado = controlAccesoRepository.save(nuevoRegistro);
        return mapToRespuestaRegistroDTO(registroGuardado);
    }

    @Transactional
    public RespuestaRegistroDTO  registrarSalida(RegistroSalidaDTO dto) {
        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));
        ControlAcceso registroAbierto = controlAccesoRepository.findFirstByEmpleadoAndHoraSalidaIsNullOrderByHoraEntradaDesc(empleado)
                .orElseThrow(()-> new BadRequestException("No hay registro de entrada abierto."));


        registroAbierto.setHoraSalida(LocalTime.now());
        registroAbierto.setObservacionSalida(dto.getObservacionSalida());

        ControlAcceso registroActualizado = controlAccesoRepository.save(registroAbierto);
        return mapToRespuestaRegistroDTO(registroActualizado);
    }

    @Transactional
    public RespuestaRegistroDTO actualizarObservacion(Integer idControl,String nuevaObservacionEntrada, String nuevaObservacionSalida) {
        ControlAcceso controlAcceso = controlAccesoRepository.findById(idControl)
                .orElseThrow(()-> new ResourceNotFoundException("Registro de control no encontrado."));
        if (nuevaObservacionEntrada != null) {
            controlAcceso.setObservacionEntrada(nuevaObservacionEntrada);
        }
        if (nuevaObservacionSalida != null) {
            controlAcceso.setObservacionSalida(nuevaObservacionSalida);
        }

        ControlAcceso actualizado = controlAccesoRepository.save(controlAcceso);
        return mapToRespuestaRegistroDTO(actualizado);
    }

    @Transactional
    public Map<String, Object> obtenerEstadoActual(Integer idEmpleado) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(()-> new ResourceNotFoundException("Empleado no encontrado."));

        Optional<ControlAcceso> registroAbierto = controlAccesoRepository.findFirstByEmpleadoAndHoraSalidaIsNullOrderByHoraEntradaDesc(empleado);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("estado", registroAbierto.isPresent() ? "En turno" : "Fuera de turno");

        registroAbierto.ifPresent(registro -> {
            respuesta.put("ultimaEntrada", registro.getHoraEntrada());
            respuesta.put("fecha", registro.getFecha());
        });

        return respuesta;
    }

    @Transactional
    public Page<RespuestaRegistroDTO> obtenerHistorial(Integer idEmpleado, Pageable pageable) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

        return controlAccesoRepository.findByEmpleadoOrderByHoraEntradaDesc(empleado, pageable).map(this::mapToRespuestaRegistroDTO);
    }

    private RespuestaRegistroDTO mapToRespuestaRegistroDTO(ControlAcceso controlAcceso) {
        String nombreCompletoEmpleado = null;
        if (controlAcceso.getEmpleado() != null) {
            nombreCompletoEmpleado = controlAcceso.getEmpleado().getNombre() + " " + controlAcceso.getEmpleado().getApellido();
        }
        return RespuestaRegistroDTO.builder()
                .idControl(controlAcceso.getIdControl())
                .idEmpleado(controlAcceso.getEmpleado() != null ? controlAcceso.getEmpleado().getIdEmpleado() : null)
                .nombre(nombreCompletoEmpleado)
                .horaEntrada(controlAcceso.getHoraEntrada())
                .horaSalida(controlAcceso.getHoraSalida())
                .observacionEntrada(controlAcceso.getObservacionEntrada())
                .observacionSalida(controlAcceso.getObservacionSalida())
                .fecha(controlAcceso.getFecha())
                .build();
    }

    public List<RespuestaRegistroDTO> listarTodos() {
        List<ControlAcceso> registros = controlAccesoRepository.findAll();
        return registros.stream()
                .map(this::mapToRespuestaRegistroDTO)
                .toList();
    }



}
