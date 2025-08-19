package com.example.chronoworks.repository;

import com.example.chronoworks.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Integer>, JpaSpecificationExecutor<Asignacion> {
    @Query("SELECT a FROM Asignacion a WHERE a.campana.idCampana = :idCampana")
    List<Asignacion> findByCampanaId(@Param("idCampana") Integer idCampana);

    @Query("SELECT a FROM Asignacion a WHERE a.campana.idCampana = :idCampana AND a.esLider = true")
    Optional<Asignacion> findLiderByCampanaId(@Param("idCampana") Integer idCampana);

    @Query("SELECT a FROM Asignacion a WHERE a.campana.idCampana = :idCampana AND a.esLider = false")
    List<Asignacion> findAgentesByCampanaId(@Param("idCampana") Integer idCampana);

    @Modifying
    @Query("DELETE FROM Asignacion a WHERE a.campana.idCampana = :idCampana")
    void deleteByCampanaIdCampana(@Param("idCampana") Integer idCampana);
}
