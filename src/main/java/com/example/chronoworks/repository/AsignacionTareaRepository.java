package com.example.chronoworks.repository;

import com.example.chronoworks.model.AsignacionTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionTareaRepository extends JpaRepository<AsignacionTarea, Integer>, JpaSpecificationExecutor<AsignacionTarea> {

    List<AsignacionTarea> findByCampanaIdCampana(Integer idCampana);

}
