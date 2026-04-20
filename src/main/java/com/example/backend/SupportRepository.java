package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportRepository extends JpaRepository<SupportRequest, Integer> {
    List<SupportRequest> findByUserIdOrderByCreatedAtDesc(Integer userId);
}