package com.example.chronoworks.repository;

import com.example.chronoworks.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Integer>, JpaSpecificationExecutor<Asignacion> {
}
