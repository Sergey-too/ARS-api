package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Integer> {
    
    List<UserCategory> findByUserId(Integer userId);
    
    boolean existsByUserIdAndName(Integer userId, String name);
}