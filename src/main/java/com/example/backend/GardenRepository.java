package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GardenRepository extends JpaRepository<Garden, Integer> {
    
    @Query("SELECT g FROM Garden g LEFT JOIN FETCH g.areas WHERE g.userId = :userId")
    List<Garden> findByUserIdWithAreas(@Param("userId") Integer userId);
    
    List<Garden> findByUserId(Integer userId);
}