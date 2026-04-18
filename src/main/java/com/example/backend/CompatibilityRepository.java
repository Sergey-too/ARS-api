package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository 
public interface CompatibilityRepository extends JpaRepository<Crop, Integer> {
    
    @Query(value = "EXEC GetCropsCompatibilityMatrix", nativeQuery = true)
    List<Object[]> getRawMatrix();
}