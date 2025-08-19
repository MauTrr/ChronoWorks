package com.example.chronoworks.controller;

import com.example.chronoworks.dto.asignacion.AsignacionCreacionDTO;
import com.example.chronoworks.dto.asignacion.FiltroAsignacionDTO;
import com.example.chronoworks.dto.asignacion.RespuestaAsignacionDTO;
import com.example.chronoworks.model.enums.AsignacionEstado;
import com.example.chronoworks.service.AsignacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AsignacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AsignacionService asignacionService;

    @InjectMocks
    private AsignacionController asignacionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(asignacionController).build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private AsignacionCreacionDTO crearAsignacionDTOValido() {
        AsignacionCreacionDTO dto = new AsignacionCreacionDTO();
        dto.setFecha(LocalDateTime.now().minusHours(1)); // Fecha en pasado
        dto.setObservaciones("Observaciones de prueba");
        dto.setEstado(AsignacionEstado.ACTIVA); // Asume que existe este enum
        dto.setIdTarea(1);
        dto.setIdCampana(1);
        dto.setIdEmpleado(1);
        return dto;
    }

    @Test
    void crearAsignacion() throws Exception {
        AsignacionCreacionDTO dto = crearAsignacionDTOValido();
        RespuestaAsignacionDTO respuesta = Mockito.mock(RespuestaAsignacionDTO.class);

        when(asignacionService.crearAsignacion(any(AsignacionCreacionDTO.class))).thenReturn(respuesta);

        mockMvc.perform(post("/api/asignaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void obtenerAsignacion() throws  Exception {
        RespuestaAsignacionDTO respuesta = Mockito.mock(RespuestaAsignacionDTO.class);

        when(asignacionService.obtenerAsignacion(anyInt())).thenReturn(respuesta);

        mockMvc.perform(get("/api/asignaciones/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listarAsignaciones() throws Exception {
        Page<RespuestaAsignacionDTO> page = new PageImpl<>(Collections.emptyList());

        when(asignacionService.listarAsignaciones(
                argThat(filter ->
                        "Juan".equals(filter.getNombreEmpleado()) &&
                                "Perez".equals(filter.getApellidoEmpleado()) &&
                                "Campaña 1".equals(filter.getNombreCampana()) &&
                                AsignacionEstado.ACTIVA.equals(filter.getEstado())
                ),
                argThat(pageable ->
                        pageable.getPageNumber() == 0 &&
                                pageable.getPageSize() == 10 &&
                                pageable.getSort().getOrderFor("idAsignacion").getDirection().isAscending()
                )
        )).thenReturn(page);

        mockMvc.perform(get("/api/asignaciones")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "idAsignacion")
                        .param("direction", "asc")
                        .param("nombreEmpleado", "Juan")
                        .param("apellidoEmpleado", "Perez")
                        .param("nombreCampana", "Campaña 1")
                        .param("estado", "ACTIVA"))
                .andExpect(status().isOk());
    }

    @Test
    void listarAsignacionesActivas() throws Exception {
        // 1. Crea un objeto real de RespuestaAsignacionDTO usando el builder
        RespuestaAsignacionDTO respuesta = RespuestaAsignacionDTO.builder()
                .idAsignacion(1)
                .fecha(LocalDateTime.now())
                .observaciones("Observación de prueba")
                .idTarea(101)
                .nombreTarea("Tarea de prueba")
                .detalles("Detalles de prueba")
                .idCampana(201)
                .nombreCampana("Campaña de prueba")
                .idEmpleado(301)
                .nombre("Empleado")
                .apellido("Prueba")
                .estado(AsignacionEstado.ACTIVA)
                .build();

        // 2. Crea un PageRequest explícito
        Pageable pageable = PageRequest.of(0, 10);

        // 3. Crea la página con el pageable
        Page<RespuestaAsignacionDTO> page = new PageImpl<>(
                List.of(respuesta),
                pageable,
                1 // total elements
        );

        // 4. Configura el mock del servicio
        when(asignacionService.listarAsignacionesActivas(any(FiltroAsignacionDTO.class), eq(pageable)))
                .thenReturn(page);

        // 5. Ejecuta la prueba
        mockMvc.perform(get("/api/asignaciones/activas")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(result -> {
                    if (result.getResolvedException() != null) {
                        result.getResolvedException().printStackTrace();
                    }
                    System.out.println("Response: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idAsignacion").value(1));
    }

    @Test
    void actualizarAsignacion() throws Exception {
        AsignacionCreacionDTO dto = crearAsignacionDTOValido();
        RespuestaAsignacionDTO respuesta = Mockito.mock(RespuestaAsignacionDTO.class);

        when(asignacionService.actualizarAsignacion(anyInt(), any(AsignacionCreacionDTO.class))).thenReturn(respuesta);

        mockMvc.perform(put("/api/asignaciones/1/actualizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

    }

}
