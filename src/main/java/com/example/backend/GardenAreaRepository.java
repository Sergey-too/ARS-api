// GardenAreaRepository.java
package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GardenAreaRepository extends JpaRepository<GardenArea, GardenAreaId> {
    
    List<GardenArea> findByGardenId(Integer gardenId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM GardenArea ga WHERE ga.gardenId = :gardenId")
    void deleteByGardenId(Integer gardenId);

    @Query("SELECT a FROM Area a JOIN GardenArea ga ON a.id = ga.areaId WHERE ga.gardenId = :gardenId")
    List<Area> findAreasByGardenId(@Param("gardenId") Integer gardenId);
}