package com.example.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "garden_history")
public class GardenHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "action_type_id", nullable = false)
    private Integer actionTypeId; // 1=Посадка, 2=Полив и т.д.
    
    @Column(name = "done_at", nullable = false)
    private LocalDateTime doneAt;
    
    @Column(name = "crop_name", length = 100)
    private String cropName;
    
    @Column(name = "variety", length = 100)
    private String variety;
    
    @Column(name = "area_name", length = 100)
    private String areaName;

    // Погодные данные
    private Double temperature;
    private Double humidity;
    private Double precipitation;

    // Интервалы (Snapshot)
    @Column(name = "watering_interval") private Integer wateringInterval;
    @Column(name = "fertilizing_interval") private Integer fertilizingInterval;
    @Column(name = "soil_care_interval") private Integer soilCareInterval;
    @Column(name = "protection_interval") private Integer protectionInterval;

    public GardenHistory() {}

    // Геттеры и сеттеры для всех полей
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    public Double getPrecipitation() { return precipitation; }
    public void setPrecipitation(Double precipitation) { this.precipitation = precipitation; }
    public Integer getWateringInterval() { return wateringInterval; }
    public void setWateringInterval(Integer wateringInterval) { this.wateringInterval = wateringInterval; }
    public Integer getFertilizingInterval() { return fertilizingInterval; }
    public void setFertilizingInterval(Integer fertilizingInterval) { this.fertilizingInterval = fertilizingInterval; }
    public Integer getSoilCareInterval() { return soilCareInterval; }
    public void setSoilCareInterval(Integer soilCareInterval) { this.soilCareInterval = soilCareInterval; }
    public Integer getProtectionInterval() { return protectionInterval; }
    public void setProtectionInterval(Integer protectionInterval) { this.protectionInterval = protectionInterval; }
}