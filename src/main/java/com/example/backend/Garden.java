package com.example.backend;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "gardens")
public class Garden {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @ManyToMany
    @JoinTable(
        name = "garden_areas",
        joinColumns = @JoinColumn(name = "garden_id"),
        inverseJoinColumns = @JoinColumn(name = "area_id")
    )
    private List<Area> areas;
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public List<Area> getAreas() { return areas; }
    public void setAreas(List<Area> areas) { this.areas = areas; }
}