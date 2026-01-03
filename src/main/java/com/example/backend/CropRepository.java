package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CropRepository extends JpaRepository<Crop, Integer> {
    
    // Найти растения по названию категории
    @Query("SELECT c FROM Crop c WHERE c.category.name = :categoryName")
    List<Crop> findByCategoryName(@Param("categoryName") String categoryName);
    
    // Найти растения по ID категории
    List<Crop> findByCategoryId(Integer categoryId);
}