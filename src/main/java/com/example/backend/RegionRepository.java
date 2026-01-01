package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
    
    @Query("SELECT r FROM Region r ORDER BY r.name")
    List<Region> findAllOrdered();
    
    Region findByName(String name);
}