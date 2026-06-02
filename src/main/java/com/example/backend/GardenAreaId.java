package com.example.backend;

import java.io.Serializable;
import java.util.Objects;

public class GardenAreaId implements Serializable {
    private Integer gardenId;
    private Integer areaId;
    
    public GardenAreaId() {}
    
    public GardenAreaId(Integer gardenId, Integer areaId) {
        this.gardenId = gardenId;
        this.areaId = areaId;
    }
    
    public Integer getGardenId() { return gardenId; }
    public void setGardenId(Integer gardenId) { this.gardenId = gardenId; }
    
    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GardenAreaId that = (GardenAreaId) o;
        return Objects.equals(gardenId, that.gardenId) &&
               Objects.equals(areaId, that.areaId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(gardenId, areaId);
    }
}