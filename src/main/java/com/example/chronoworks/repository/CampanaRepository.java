package com.example.chronoworks.repository;

import com.example.chronoworks.model.Campana;
import com.example.chronoworks.model.enums.CampanaEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampanaRepository extends JpaRepository<Campana, Integer> {

    Optional<Campana> findByNombreCampana(String nombreCampana);

    List<Campana> findByNombreCampanaContainingIgnoreCaseAndEstadoIn(String nombreCampana, List<CampanaEstado> estados);

    List<Campana> findByEstado(CampanaEstado estado);

    List<Campana> findByEstadoIn(List<CampanaEstado> estados);
}
