package com.example.chronoworks.repository;

import com.example.chronoworks.model.AsignacionCampana;
import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import com.example.chronoworks.model.enums.CampanaEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AsignacionCampanaRepository extends JpaRepository<AsignacionCampana, Integer> {

    @Query("SELECT a FROM AsignacionCampana a WHERE a.campana.idCampana = :idCampana")
    List<AsignacionCampana> findByCampanaId(@Param("idCampana") Integer idCampana);

    @Query("SELECT a FROM AsignacionCampana a WHERE a.campana.idCampana = :idCampana AND a.esLider = true")
    Optional<AsignacionCampana> findLiderByCampanaId(@Param("idCampana") Integer idCampana);

    @Query("SELECT a FROM AsignacionCampana a WHERE a.campana.idCampana = :idCampana AND a.esLider = false")
    List<AsignacionCampana> findAgentesByCampanaId(@Param("idCampana") Integer idCampana);

    @Modifying
    @Query("DELETE FROM AsignacionCampana a WHERE a.campana.idCampana = :idCampana")
    void deleteByCampanaIdCampana(@Param("idCampana") Integer idCampana);

    List<AsignacionCampana> findByEmpleadoIdEmpleadoAndEstado(Integer idEmpleado, AsignacionCampanaEstado estado);

    Optional<AsignacionCampana> findByEmpleadoIdEmpleadoAndEsLiderAndEstadoAndCampanaEstadoIn(
            Integer idEmpleado,
            Boolean esLider,
            AsignacionCampanaEstado estado,
            List<CampanaEstado> estadosCampana
    );

}
