package com.example.backend;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByLogin(String login);
    
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.login = :identifier")
    Optional<User> findByIdentifier(@Param("identifier") String identifier);
    
    boolean existsByEmail(String email);
    
    boolean existsByLogin(String login);
}