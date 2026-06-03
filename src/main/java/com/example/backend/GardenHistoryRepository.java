package com.example.backend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GardenHistoryRepository extends JpaRepository<GardenHistory, Integer> {

    @Query("SELECT h FROM GardenHistory h WHERE h.actionTypeId = 1 AND " +
           "h.cropName = :cropName AND h.variety = :variety AND h.areaName = :areaName")
    Optional<GardenHistory> findPlantingByCropVarietyArea(
        @Param("cropName") String cropName,
        @Param("variety") String variety,
        @Param("areaName") String areaName
    );
    
    // ИЗМЕНЕНО: возвращает список, а не Optional
    @Query("SELECT h FROM GardenHistory h WHERE " +
           "h.cropName = :cropName AND " +
           "h.variety = :variety AND " +
           "h.areaName = :areaName AND " +
           "h.actionTypeId = :actionTypeId " +
           "ORDER BY h.doneAt DESC")
    List<GardenHistory> findLastActionsByCropVarietyAreaAndType(
        @Param("cropName") String cropName,
        @Param("variety") String variety,
        @Param("areaName") String areaName,
        @Param("actionTypeId") Integer actionTypeId
    );

    @Query("SELECT h FROM GardenHistory h WHERE h.actionTypeId = 1 AND " +
           "h.userId = :userId")
    List<GardenHistory> findAllPlantingsByUserId(@Param("userId") Integer userId);

    @Query("SELECT h FROM GardenHistory h WHERE h.userId = :userId ORDER BY h.doneAt DESC")
    List<GardenHistory> findAllByUserId(@Param("userId") Integer userId);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM GardenHistory h " +
           "WHERE h.userId = :userId AND h.areaName = :areaName AND " +
           "h.cropName = :cropName AND h.variety = :variety AND h.actionTypeId = :actionTypeId")
    boolean existsByUserIdAndAreaNameAndCropNameAndVarietyAndActionTypeId(
        @Param("userId") Integer userId, 
        @Param("areaName") String areaName, 
        @Param("cropName") String cropName,
        @Param("variety") String variety,
        @Param("actionTypeId") Integer actionTypeId
    );

    @Query("SELECT DISTINCT h.cropName FROM GardenHistory h WHERE h.actionTypeId = 1 AND h.userId = :userId")
        List<String> findAllPlantedCropNamesByUserId(@Param("userId") Integer userId);
        
        @Query("SELECT h FROM GardenHistory h WHERE h.userId = :userId AND h.gardenName = :gardenName ORDER BY h.doneAt DESC")
        List<GardenHistory> findAllByUserIdAndGardenName(
            @Param("userId") Integer userId, 
            @Param("gardenName") String gardenName
        );

        List<GardenHistory> findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
        Integer userId,
        String cropName,
        String areaName,
        Integer actionTypeId,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );
}