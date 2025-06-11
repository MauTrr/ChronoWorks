package com.example.chronoworks.repository;

import com.example.chronoworks.model.Campana;
import com.example.chronoworks.model.enums.CampanaEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampanaRepository extends JpaRepository<Campana, Integer> {

    Optional<Campana> findByNombreCampana(String nombreCampana);

    List<Campana> findByNombreCampanaContainingIgnoreCase(String nombreEmpresa);

    List<Campana> findByEstado(CampanaEstado estado);
}
