package com.example.chronoworks.service;

import com.example.chronoworks.dto.empresa.EmpresaDTO;
import com.example.chronoworks.dto.empresa.FiltroEmpresaDTO;
import com.example.chronoworks.dto.empresa.RespuestaEmpresaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.IllegalStateException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Empresa;
import com.example.chronoworks.repository.EmpresaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public RespuestaEmpresaDTO registrarEmpresa(EmpresaDTO dto) {
        if (empresaRepository.findByNombreEmpresa(dto.getNombreEmpresa()).isPresent()) {
            throw new BadRequestException("La empresa ya está registrada");
        }
        if(!dto.getTelefono().matches("^[0-9]{10}$")){
            throw new IllegalStateException("Formato de telefono invalido");
        }

        Empresa empresa = new Empresa();
        empresa.setNombreEmpresa(dto.getNombreEmpresa());
        empresa.setNitEmpresa(dto.getNitEmpresa());
        empresa.setDireccion(dto.getDireccion());
        empresa.setTelefono(dto.getTelefono());
        empresa.setSector(dto.getSector());
        empresa.setRepresentante(dto.getRepresentante());
        empresa.setActivo(true);
        return mapToEmpresaDTO(empresaRepository.save(empresa));
    }

    @Transactional(readOnly = true)
    public RespuestaEmpresaDTO obtenerEmpresa(Integer id) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        return mapToEmpresaDTO(empresa);
    }

    @Transactional(readOnly = true)
    public Page<RespuestaEmpresaDTO> listarEmpresas(FiltroEmpresaDTO filtro, Pageable pageable) {
        Specification<Empresa> spec = crearSpecificationEmpresa(filtro);
        Page<Empresa> empresasPage = empresaRepository.findAll(spec, pageable);
        return empresasPage.map(this::mapToEmpresaDTO);
    }

    private Specification<Empresa> crearSpecificationEmpresa(FiltroEmpresaDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por Nombre de la empresa
            if (filtro.getNombreEmpresa() != null && !filtro.getNombreEmpresa().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nombreEmpresa")), "%" + filtro.getNombreEmpresa().toLowerCase() + "%"));
            }

            // Filtro por rol
            if (filtro.getSector() != null && !filtro.getSector().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sector")), "%" + filtro.getSector().toLowerCase() + "%"));
            }

            // Filtro por estado activo
            if (filtro.getActivo() != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), filtro.getActivo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public RespuestaEmpresaDTO actualizarEmpresa(Integer id, EmpresaDTO dto) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        empresa.setNombreEmpresa(dto.getNombreEmpresa());
        empresa.setNitEmpresa(dto.getNitEmpresa());
        empresa.setDireccion(dto.getDireccion());
        empresa.setTelefono(dto.getTelefono());
        empresa.setSector(dto.getSector());
        empresa.setRepresentante(dto.getRepresentante());
        return mapToEmpresaDTO(empresaRepository.save(empresa));
    }

    @Transactional
    public void desactivarEmpresa(Integer idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        empresa.setActivo(false);
        empresaRepository.save(empresa);
    }



    private RespuestaEmpresaDTO mapToEmpresaDTO(Empresa empresa) {
        return RespuestaEmpresaDTO.builder()
                .idEmpresa(empresa.getIdEmpresa())
                .nombreEmpresa(empresa.getNombreEmpresa())
                .nitEmpresa(empresa.getNitEmpresa())
                .direccion(empresa.getDireccion())
                .telefono(empresa.getTelefono())
                .sector(empresa.getSector())
                .representante(empresa.getRepresentante())
                .activo(empresa.isActivo())
                .build();
    }
}