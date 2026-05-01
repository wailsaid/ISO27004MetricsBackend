package com.pfem2.iso27004.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pfem2.iso27004.Entity.MeasurementProgramme;

@Repository
public interface MeasurementProgrammeRepository extends JpaRepository<MeasurementProgramme, Long> {

    List<MeasurementProgramme> findByStatus(String status);

    @Query("SELECT p FROM MeasurementProgramme p ORDER BY p.createdAt DESC")
    List<MeasurementProgramme> findAllOrderedByDate();
}
