package com.example.chronoworks.repository;

import com.example.chronoworks.model.Campaña;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Empresa;
import com.example.chronoworks.model.enums.CampañaEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampañaRepository extends JpaRepository<Campaña, Integer> {

    Optional<Campaña> findByNombreCampaña(String nombreCampaña);

    List<Campaña> findByNombreCampañaContainingIgnoreCase(String nombreEmpresa);

    List<Campaña> findByEstado(CampañaEstado estado);
}
