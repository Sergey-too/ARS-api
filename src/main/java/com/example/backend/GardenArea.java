package com.example.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "garden_areas")
@IdClass(GardenAreaId.class)
public class GardenArea {
    
    @Id
    @Column(name = "garden_id")
    private Integer gardenId;
    
    @Id
    @Column(name = "area_id")
    private Integer areaId;
    
    @ManyToOne
    @JoinColumn(name = "garden_id", insertable = false, updatable = false)
    private Garden garden;
    
    @ManyToOne
    @JoinColumn(name = "area_id", insertable = false, updatable = false)
    private Area area;
    
    // Getters and Setters
    public Integer getGardenId() { return gardenId; }
    public void setGardenId(Integer gardenId) { this.gardenId = gardenId; }
    
    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }
    
    public Garden getGarden() { return garden; }
    public void setGarden(Garden garden) { this.garden = garden; }
    
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
}