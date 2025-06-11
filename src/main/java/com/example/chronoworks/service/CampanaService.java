package com.example.chronoworks.service;

import com.example.chronoworks.dto.campana.CampanaDTO;
import com.example.chronoworks.dto.campana.RespuestaCampanaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Campana;
import com.example.chronoworks.model.Empresa;
import com.example.chronoworks.model.enums.CampanaEstado;
import com.example.chronoworks.repository.CampanaRepository;
import com.example.chronoworks.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampanaService {

    private final CampanaRepository campanaRepository;
    private final EmpresaRepository empresaRepository;

    public CampanaService(CampanaRepository campanaRepository,
                          EmpresaRepository empresaRepository) {
        this.campanaRepository = campanaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public RespuestaCampanaDTO crearCampana(RespuestaCampanaDTO dto) {
        if(campanaRepository.findByNombreCampana(dto.getNombreCampana()).isPresent()) {
            throw new BadRequestException("El nombre de la campaña ya esta en uso");
        }

        Empresa empresa = empresaRepository.findById(dto.getIdEmpresa())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + dto.getIdEmpresa() + " no encontrado."));

        Campana nuevaCampana = new Campana();
        nuevaCampana.setNombreCampana(dto.getNombreCampana());
        nuevaCampana.setDescripcion(dto.getDescripcion());
        nuevaCampana.setFechaInicio(dto.getFechaInicio());
        nuevaCampana.setFechaFin(dto.getFechaFin());
        nuevaCampana.setEmpresa(empresa);
        nuevaCampana.setEstado(dto.getEstado());

        Campana campanaGuardada = campanaRepository.save(nuevaCampana);

        return mapToRespuestaCampanaDTO(campanaGuardada);
    }

    @Transactional(readOnly = true)
    public RespuestaCampanaDTO obtenerCampana(Integer idCampana) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException( "campaña con ID " + idCampana + " no encontrada"));
        return mapToRespuestaCampanaDTO(campana);
    }

    @Transactional(readOnly = true)
    public List<RespuestaCampanaDTO> listarCampanas() {
        List<CampanaEstado> estadosVisibles = Arrays.asList(CampanaEstado.PLANEADA, CampanaEstado.ACTIVA);
        return campanaRepository.findByEstadoIn(estadosVisibles).stream().map(this::mapToRespuestaCampanaDTO).collect(Collectors.toList());
    }

    @Transactional
    public RespuestaCampanaDTO actualizarCampana(Integer idCampana, CampanaDTO dto, CampanaEstado nuevoEstado) {
        Campana campanaExistente = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña con ID " + idCampana + " no encontrada"));
        if(dto.getNombreCampana() != null && dto.getNombreCampana().equals(campanaExistente.getNombreCampana())){
            throw new BadRequestException("El nuevo nombre de la campaña '" + dto.getNombreCampana() + "' ya esta en uso");
        }

        campanaExistente.setNombreCampana(dto.getNombreCampana());

        if(dto.getNombreCampana() != null) campanaExistente.setNombreCampana(dto.getNombreCampana());
        if(dto.getDescripcion() != null) campanaExistente.setDescripcion(dto.getDescripcion());
        if(dto.getFechaInicio() != null) campanaExistente.setFechaInicio(dto.getFechaInicio());
        if(dto.getFechaFin() != null) campanaExistente.setFechaFin(dto.getFechaFin());
        if(dto.getIdEmpresa() != null) {
            Empresa nuevaEmpresa = empresaRepository.findById(dto.getIdEmpresa())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + dto.getIdEmpresa() + " no encontrada"));
        }

        Campana campanaActualizada = campanaRepository.save(campanaExistente);
        return mapToRespuestaCampanaDTO(campanaActualizada);
    }

    @Transactional RespuestaCampanaDTO actualizarEstadosCampana(Integer idCampana, CampanaEstado nuevoEstado) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña con ID " + idCampana + " no encontrada."));

        if ((campana.getEstado() == CampanaEstado.FINALIZADA || campana.getEstado() == CampanaEstado.CANCELADA)
                && (nuevoEstado == CampanaEstado.ACTIVA || nuevoEstado == CampanaEstado.PLANEADA)) {
            throw new BadRequestException("No se puede cambiar el estado de una campaña finalizada o cancelada a activa o planificada.");
        }

        if (campana.getEstado() == nuevoEstado) {
            throw new BadRequestException("La campaña con ID " + idCampana + " ya se encuentra en el estado " + nuevoEstado + ".");
        }

        if (nuevoEstado == CampanaEstado.FINALIZADA && campana.getFechaFin() == null) {
            campana.setFechaFin(LocalDate.now());
        }

        campana.setEstado(nuevoEstado);
        Campana campanaActualizada = campanaRepository.save(campana);
        return mapToRespuestaCampanaDTO(campanaActualizada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaCampanaDTO> buscarCampanaporEstado(CampanaEstado estado) {
        return  campanaRepository.findByEstado(estado).stream()
                .map(this::mapToRespuestaCampanaDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<RespuestaCampanaDTO> buscarCampanaPorNombre(String nombreFiltro) {
        if (nombreFiltro == null || nombreFiltro.trim().isEmpty()) {
            return listarCampanas();
        }
        List<CampanaEstado> estadosVisibles = Arrays.asList(CampanaEstado.PLANEADA, CampanaEstado.ACTIVA);
        return campanaRepository.findByNombreCampanaContainingIgnoreCaseAndEstadoIn(nombreFiltro, estadosVisibles).stream()
                .map(this::mapToRespuestaCampanaDTO)
                .collect(Collectors.toList());
    }

    private RespuestaCampanaDTO mapToRespuestaCampanaDTO(Campana campana) {
        return  RespuestaCampanaDTO.builder()
                .idCampana(campana.getIdCampana())
                .nombreCampana(campana.getNombreCampana())
                .descripcion(campana.getDescripcion())
                .fechaInicio(campana.getFechaInicio())
                .fechaFin(campana.getFechaFin())
                .idEmpresa(campana.getEmpresa()!= null ? campana.getEmpresa().getIdEmpresa() : null)
                .nombreEmpresa(campana.getNombreCampana()!= null ? campana.getEmpresa().getNombreEmpresa():null)
                .build();
    }
}
