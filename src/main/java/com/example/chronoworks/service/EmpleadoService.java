package com.example.chronoworks.service;


import com.example.chronoworks.dto.empleado.ActualizarEmpleadoDTO;
import com.example.chronoworks.dto.empleado.RegistrarEmpleadoDTO;
import com.example.chronoworks.dto.empleado.RespuestaEmpleadoDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Rol;
import com.example.chronoworks.model.Turno;
import com.example.chronoworks.repository.CredencialRepository;
import com.example.chronoworks.repository.EmpleadoRepository;
import com.example.chronoworks.repository.RolRepository;
import com.example.chronoworks.repository.TurnoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final CredencialRepository credencialRepository;
    private final RolRepository rolRepository;
    private final TurnoRepository turnoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(EmpleadoRepository empleadoRepository,
                           CredencialRepository credencialRepository,
                           RolRepository rolRepository,
                           TurnoRepository turnoRepository,
                           PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.credencialRepository = credencialRepository;
        this.rolRepository = rolRepository;
        this.turnoRepository = turnoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RespuestaEmpleadoDTO crearEmpleado(RegistrarEmpleadoDTO dto) {
        //Aca estan las validaciones para el registro de empleados
        if(empleadoRepository.findByCorreo(dto.getCorreo()).isPresent()) {
            throw new BadRequestException("El correo electrónico ya esta registrado");
        }
        if(credencialRepository.findByUsuario(dto.getUsuario()).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya esta en uso");
        }

        //Este bloque es para buscar las entidades relacionadas
        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol con ID " + dto.getIdRol() + " no encontrado."));
        Turno turno = turnoRepository.findById(dto.getIdTurno())
                .orElseThrow(() -> new ResourceNotFoundException("Turno con ID " + dto.getIdTurno() + " no encontrado."));

        //Crear empleado
        Empleado empleado = new Empleado();
        empleado.setNombre(dto.getNombre());
        empleado.setApellido(dto.getApellido());
        empleado.setCorreo(dto.getCorreo());
        empleado.setTelefono(dto.getTelefono());
        empleado.setTurno(turno);

        //Crear credencial
        Credencial credencial = new Credencial();
        credencial.setUsuario(dto.getUsuario());
        credencial.setContraseña(dto.getContraseña());
        credencial.setRol(rol);
        credencial.setEmpleado(empleado);

        //Relacion Uno a Uno desde credencial y desde empleado
        empleado.setCredencial(credencial);

        //Guarda el empleado
        Empleado empleadoGuardado  = empleadoRepository.save(empleado);

        //Mapea al DTO de respuesta y regresa
        return mapToRespuestaEmpleadoDTO(empleadoGuardado);
    }

    @Transactional(readOnly = true)
    public RespuestaEmpleadoDTO obtenerEmpleado(Integer idEmpleado) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException( "Empleado con ID " + idEmpleado + " no encontrado"));
        return mapToRespuestaEmpleadoDTO(empleado);
    }

    @Transactional(readOnly = true)
    public List<RespuestaEmpleadoDTO> listarEmpleados() {
        return empleadoRepository.findAll().stream().map(this::mapToRespuestaEmpleadoDTO).collect(Collectors.toList());
    }

    @Transactional
    public RespuestaEmpleadoDTO actualizarEmpleado(Integer idEmpleado, ActualizarEmpleadoDTO dto) {
        Empleado empleadoExistente = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException( "Empleado con ID " + idEmpleado + " no encontrado"));

        if(dto.getNombre() != null) empleadoExistente.setNombre(dto.getNombre());
        if(dto.getApellido() !=null) empleadoExistente.setApellido(dto.getApellido());
        if(dto.getCorreo() !=null) {
            empleadoRepository.findByCorreo(dto.getCorreo()).ifPresent(e ->{
                if (!e.getIdEmpleado().equals(idEmpleado)) {
                    throw new BadRequestException("El correo electronico ya esta registrado por otro empleado");
                }
            });
            empleadoExistente.setCorreo(dto.getCorreo());
        }
        if(dto.getTelefono() !=null) empleadoExistente.setTelefono(dto.getTelefono());
        if(dto.getIdTurno() !=null) {
            Turno nuevoTurno = turnoRepository.findById(dto.getIdTurno())
                    .orElseThrow(() -> new ResourceNotFoundException("Turno con ID " + dto.getIdTurno() + " no encontrado"));
            empleadoExistente.setTurno(nuevoTurno);
        }
        Empleado empleadoActualizado = empleadoRepository.save(empleadoExistente);
        return mapToRespuestaEmpleadoDTO(empleadoActualizado);
    }

    @Transactional
    public void eliminarEmpleado(Integer idEmpleado) {
        if (!empleadoRepository.existsById(idEmpleado)){
            throw new ResourceNotFoundException("Empleado con ID " + idEmpleado + " no se ha encontrado para eliminar");
        }
        empleadoRepository.deleteById(idEmpleado);
    }


    private RespuestaEmpleadoDTO mapToRespuestaEmpleadoDTO(Empleado empleado) {
        return RespuestaEmpleadoDTO.builder()
                .idEmpleado(empleado.getIdEmpleado())
                .nombre(empleado.getNombre())
                .apellido(empleado.getApellido())
                .correo(empleado.getCorreo())
                .telefono(empleado.getTelefono())
                .idTurno(empleado.getTurno() != null ? empleado.getTurno().getIdTurno() : null)
                .usuario(empleado.getCredencial() != null ? empleado.getCredencial().getUsuario() : null)
                .nombreRol(empleado.getCredencial() != null && empleado.getCredencial().getRol() != null ? empleado.getCredencial().getRol().getNombreRol() : null)
                .build();

    }
}
