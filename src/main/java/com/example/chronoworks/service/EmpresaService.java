package com.example.chronoworks.service;

import com.example.chronoworks.dto.empresa.EmpresaDTO;
import com.example.chronoworks.dto.empresa.RespuestaEmpresaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Empresa;
import com.example.chronoworks.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public RespuestaEmpresaDTO registrarEmpresa(RespuestaEmpresaDTO dto) {
        if (empresaRepository.findByNombreEmpresa(dto.getNombreEmpresa()).isPresent()) {
            throw new BadRequestException("La empresa con el nombre " + dto.getIdEmpresa() + " ya esta registrada");
        }

        Empresa nuevaEmpresa = new Empresa();
        nuevaEmpresa.setNombreEmpresa(dto.getNombreEmpresa());
        nuevaEmpresa.setDireccion(dto.getDireccion());
        nuevaEmpresa.setTelefono(dto.getTelefono());
        nuevaEmpresa.setSector(dto.getSector());
        nuevaEmpresa.setEncargado(dto.getEncargado());

        Empresa empresaGuardada = empresaRepository.save(nuevaEmpresa);

        return mapToRespuestaEmpresaDTO(empresaGuardada);
    }

    @Transactional(readOnly = true)
    public RespuestaEmpresaDTO obtenerEmpresa(Integer idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException ("Empresa con ID " + idEmpresa + " no encontrada."));
        return mapToRespuestaEmpresaDTO(empresa);
    }

    @Transactional(readOnly = true)
    public List<RespuestaEmpresaDTO> listarEmpresas() {
        return empresaRepository.findAll().stream().map(this::mapToRespuestaEmpresaDTO).collect(Collectors.toList());
    }

    @Transactional
    public RespuestaEmpresaDTO actualizarEmpresa(Integer idEmpresa, EmpresaDTO dto) {
        Empresa empresaExistente = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + idEmpresa + " no encontrada."));
        if (dto.getNombreEmpresa() != null && !dto.getNombreEmpresa().equals(empresaExistente.getNombreEmpresa())){
            if (empresaRepository.findByNombreEmpresa(dto.getNombreEmpresa()).isPresent()) {
                throw new BadRequestException("El nuevo nombre de empresa '" + dto.getNombreEmpresa() + "' ya esta en uso");
            }
            empresaExistente.setNombreEmpresa(dto.getNombreEmpresa());
        }
        if (dto.getDireccion()!= null) empresaExistente.setDireccion(dto.getDireccion());
        if (dto.getTelefono()!= null) empresaExistente.setTelefono(dto.getTelefono());
        if (dto.getSector()!= null) empresaExistente.setSector(dto.getSector());
        if (dto.getEncargado()!= null) empresaExistente.setEncargado(dto.getEncargado());

        Empresa empresaActualizada = empresaRepository.save(empresaExistente);
        return mapToRespuestaEmpresaDTO(empresaActualizada);
    }

    @Transactional
    public void eliminarEmpresa(Integer idEmpresa) {
        if (!empresaRepository.existsById(idEmpresa)){
            throw new ResourceNotFoundException("Empresa con ID" + idEmpresa + "no se ha encontrado para eliminar.");
        }
        empresaRepository.deleteById(idEmpresa);
    }

    @Transactional(readOnly = true)
    public List<RespuestaEmpresaDTO> buscarEmpresasPorNombre(String nombreFiltro) {
        if (nombreFiltro == null || nombreFiltro.trim().isEmpty()) {
            return  listarEmpresas();
        }
        return empresaRepository.findByNombreEmpresaContainingIgnoreCase(nombreFiltro).stream()
                .map(this::mapToRespuestaEmpresaDTO)
                .collect(Collectors.toList());
    }

    private  RespuestaEmpresaDTO mapToRespuestaEmpresaDTO(Empresa empresa) {
        return RespuestaEmpresaDTO.builder()
                .idEmpresa(empresa.getIdEmpresa())
                .nombreEmpresa(empresa.getNombreEmpresa())
                .direccion(empresa.getDireccion())
                .telefono(empresa.getTelefono())
                .sector(empresa.getSector())
                .encargado(empresa.getEncargado())
                .build();
    }
}
