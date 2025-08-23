package com.example.chronoworks.repository;

import com.example.chronoworks.model.Campana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampanaRepository extends JpaRepository<Campana, Integer>, JpaSpecificationExecutor<Campana> {

    List<Campana> findByNombreCampana(String nombreCampana);

}
