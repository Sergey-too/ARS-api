package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GardenHistoryRepository extends JpaRepository<GardenHistory, Integer> {

    boolean existsByCropNameAndActionTypeId(String cropName, Integer actionTypeId);

    @Query("SELECT h FROM GardenHistory h WHERE h.actionTypeId = 1 AND " +
           "(h.cropName IN (SELECT c.name FROM Crop c JOIN UserCrop uc ON uc.cropId = c.id WHERE uc.userId = :userId) OR " +
           " h.cropName IN (SELECT ic.name FROM IndividualUserCrop ic JOIN UserCrop uc ON uc.individualCropId = ic.id WHERE uc.userId = :userId))")
    List<GardenHistory> findAllPlantingsByUserId(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT h.cropName FROM GardenHistory h WHERE h.actionTypeId = 1")
    List<String> findAllPlantedCropNames();

    Optional<GardenHistory> findTopByCropNameAndVarietyAndActionTypeIdOrderByDoneAtDesc(
        String cropName, String variety, Integer actionTypeId
    );
}