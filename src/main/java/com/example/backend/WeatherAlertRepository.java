package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Integer> {
    
    @Query("SELECT wa FROM WeatherAlert wa WHERE wa.isActive = true AND wa.regionId IN :regionIds")
    List<WeatherAlert> findActiveAlertsForRegions(@Param("regionIds") List<Integer> regionIds);
}