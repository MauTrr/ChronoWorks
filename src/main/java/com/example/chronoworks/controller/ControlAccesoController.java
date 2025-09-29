package com.example.chronoworks.controller;

import com.example.chronoworks.dto.controlacceso.RegistroEntradaDTO;
import com.example.chronoworks.dto.controlacceso.RegistroSalidaDTO;
import com.example.chronoworks.dto.controlacceso.RespuestaRegistroDTO;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.service.ControlAccesoService;
import com.example.chronoworks.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/control-acceso")
public class ControlAccesoController {

    private final ControlAccesoService controlAccesoService;
    private final EmpleadoService empleadoService;

    public ControlAccesoController(ControlAccesoService controlAccesoService, EmpleadoService empleadoService) {
        this.controlAccesoService = controlAccesoService;
        this.empleadoService = empleadoService;
    }

    // === ENTRADA ===
    @PostMapping("/entrada")
    public ResponseEntity<RespuestaRegistroDTO> registrarEntrada(
            Principal principal,
            @RequestBody(required = false) RegistroEntradaDTO dto) {

        String username = principal.getName();
        System.out.println(">>> Usuario autenticado desde Principal: " + username);

        Empleado empleado = empleadoService.findByUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Empleado no encontrado"));

        RegistroEntradaDTO payload = dto != null ? dto : new RegistroEntradaDTO();
        payload.setIdEmpleado(empleado.getIdEmpleado());

        RespuestaRegistroDTO respuesta = controlAccesoService.registrarEntrada(payload);

        // 🔹 estado actualizado
        respuesta.setEstado("En turno");

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // === SALIDA ===
    @PostMapping("/salida")
    public ResponseEntity<RespuestaRegistroDTO> registrarSalida(
            Principal principal,
            @RequestBody(required = false) RegistroSalidaDTO dto) {

        String username = principal.getName();
        Empleado empleado = empleadoService.findByUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Empleado no encontrado"));

        RegistroSalidaDTO payload = dto != null ? dto : new RegistroSalidaDTO();
        payload.setIdEmpleado(empleado.getIdEmpleado());

        RespuestaRegistroDTO respuesta = controlAccesoService.registrarSalida(payload);

        // 🔹 estado actualizado
        respuesta.setEstado("Fuera de turno");

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // === ACTUALIZAR OBSERVACIONES ===
    @PatchMapping("/{idControl}/observaciones")
    public ResponseEntity<RespuestaRegistroDTO> actualizarObservaciones(
            @PathVariable Integer idControl,
            @RequestParam(required = false) String observacionEntrada,
            @RequestParam(required = false) String observacionSalida) {

        RespuestaRegistroDTO respuesta = controlAccesoService.actualizarObservacion(
                idControl, observacionEntrada, observacionSalida);

        return ResponseEntity.ok(respuesta);
    }

    // === ESTADO ACTUAL DEL EMPLEADO ===
    @GetMapping("/empleados/{idEmpleado}/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstadoActual(
            @PathVariable Integer idEmpleado) {
        return ResponseEntity.ok(controlAccesoService.obtenerEstadoActual(idEmpleado));
    }

    // === HISTORIAL DE UN EMPLEADO ===
    @GetMapping("/empleados/{idEmpleado}/historial")
    public ResponseEntity<Page<RespuestaRegistroDTO>> obtenerHistorial(
            @PathVariable Integer idEmpleado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RespuestaRegistroDTO> historial = controlAccesoService.obtenerHistorial(idEmpleado, pageable);
        return ResponseEntity.ok(historial);
    }

    // === LISTAR TODOS LOS REGISTROS ===
    @GetMapping
    public ResponseEntity<List<RespuestaRegistroDTO>> listarTodosLosRegistros() {
        return ResponseEntity.ok(controlAccesoService.listarTodos());
    }

    // === INFO DEL USUARIO AUTENTICADO ===
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Principal principal) {
        String username = principal.getName();
        System.out.println(">>> Consultando /me para el usuario autenticado: " + username);

        Empleado empleado = empleadoService.findByUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Empleado no encontrado"));

        return ResponseEntity.ok(Map.of(
                "idEmpleado", empleado.getIdEmpleado(),
                "nombre", empleado.getNombre(),
                "correo", empleado.getCorreo(),
                "rol", empleado.getCredencial() != null && empleado.getCredencial().getRol() != null
                        ? empleado.getCredencial().getRol().getNombreRol()
                        : null
        ));
    }


}
