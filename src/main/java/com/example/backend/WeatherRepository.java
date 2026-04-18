package com.example.backend;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Integer> {
    
    @Query("SELECT w FROM Weather w WHERE w.regionId = :regionId ORDER BY w.date ASC")
    List<Weather> findByRegionId(@Param("regionId") Integer regionId);
    
    @Query("SELECT w FROM Weather w WHERE w.regionId = :regionId AND w.date >= :startDate ORDER BY w.date ASC")
    List<Weather> findByRegionIdAndDateAfter(@Param("regionId") Integer regionId, 
                                             @Param("startDate") LocalDate startDate);
    
    @Query("SELECT w FROM Weather w WHERE w.regionId = :regionId AND w.date = :date")
    Weather findByRegionIdAndDate(@Param("regionId") Integer regionId, 
                                  @Param("date") LocalDate date);

    @Query(value = "EXEC GetWeatherComparison :regionId", nativeQuery = true)
    Map<String, Object> getWeatherComparison(@Param("regionId") Integer regionId);

    @Query(value = "EXEC GetYearlyWeatherComparison @regId = :regionId", nativeQuery = true)
    List<Object[]> getYearlyComparisonRaw(@Param("regionId") Long regionId);
}