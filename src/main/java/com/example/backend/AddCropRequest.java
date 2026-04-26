package com.example.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddCropRequest {

    @JsonProperty("individualCropId")
    private Integer cropId;

    @JsonProperty("userId")
    private Integer userId;

    @JsonProperty("areaId")
    private Integer areaId;

    // Пустой конструктор
    public AddCropRequest() {}

    // Геттеры и сеттеры
    public Integer getCropId() { 
        return cropId; 
    }
    
    public void setCropId(Integer cropId) { 
        this.cropId = cropId; 
    }

    public Integer getUserId() { 
        return userId; 
    }
    
    public void setUserId(Integer userId) { 
        this.userId = userId; 
    }

    public Integer getAreaId() { 
        return areaId; 
    }
    
    public void setAreaId(Integer areaId) { 
        this.areaId = areaId; 
    }
} 