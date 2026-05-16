package com.example.backend;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GardenTaskDto {
    private Integer userCropId;
    private String cropName;
    private String variety;
    private Integer areaId;
    private String areaName;
    private Integer actionTypeId;
    private String actionName;
    private LocalDate dueDate;
    private LocalDateTime lastDoneAt;
    private Boolean isOverdue;
    
    public GardenTaskDto(Integer userCropId, String cropName, String variety,
                         Integer areaId, String areaName, Integer actionTypeId,
                         String actionName, LocalDate dueDate, LocalDateTime lastDoneAt) {
        this.userCropId = userCropId;
        this.cropName = cropName;
        this.variety = variety;
        this.areaId = areaId;
        this.areaName = areaName;
        this.actionTypeId = actionTypeId;
        this.actionName = actionName;
        this.dueDate = dueDate;
        this.lastDoneAt = lastDoneAt;
        this.isOverdue = dueDate != null && dueDate.isBefore(LocalDate.now()) && lastDoneAt == null;
    }
    
    public Integer getUserCropId() { return userCropId; }
    public String getCropName() { return cropName; }
    public String getVariety() { return variety; }
    public Integer getAreaId() { return areaId; }
    public String getAreaName() { return areaName; }
    public Integer getActionTypeId() { return actionTypeId; }
    public String getActionName() { return actionName; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getLastDoneAt() { return lastDoneAt; }
    public Boolean getIsOverdue() { return isOverdue; }
}