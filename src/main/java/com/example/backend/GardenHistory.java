package com.example.backend;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "garden_history")
public class GardenHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "action_type_id", nullable = false)
    private Integer actionTypeId;
    
    @Column(name = "done_at", nullable = false)
    private LocalDateTime doneAt;
    
    @Column(name = "crop_name")
    private String cropName;
    
    @Column(name = "variety")
    private String variety;
    
    @Column(name = "area_name")
    private String areaName;

    @Column(name = "garden_name")
    private String gardenName;

    @Column(name = "region_id")
    private Integer regionId;

    public GardenHistory() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public Integer getActionTypeId() { return actionTypeId; }
    public void setActionTypeId(Integer actionTypeId) { this.actionTypeId = actionTypeId; }
    
    public LocalDateTime getDoneAt() { return doneAt; }
    public void setDoneAt(LocalDateTime doneAt) { this.doneAt = doneAt; }
    
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    
    public String getVariety() { return variety; }
    public void setVariety(String variety) { this.variety = variety; }
    
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    
    public String getGardenName() { return gardenName; }  
    public void setGardenName(String gardenName) { this.gardenName = gardenName; }  
    
    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }
}