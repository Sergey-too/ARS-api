package com.example.backend;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "weather")
public class Weather {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "region_id")
    private Integer regionId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "temperature", length = 10)
    private String temperature;
    
    @Column(name = "humidity", length = 10)
    private String humidity;
    
    @Column(name = "precipitation", length = 25)
    private String precipitation;
    
    @Column(name = "wind", length = 25)
    private String wind;
    
    @Column(name = "condition", length = 50)
    private String condition;
    
    // Конструкторы
    public Weather() {}
    
    public Weather(Integer regionId, LocalDate date, String temperature, 
                  String humidity, String precipitation, String wind, String condition) {
        this.regionId = regionId;
        this.date = date;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipitation = precipitation;
        this.wind = wind;
        this.condition = condition;
    }
    
    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }
    
    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }
    
    public String getPrecipitation() { return precipitation; }
    public void setPrecipitation(String precipitation) { this.precipitation = precipitation; }
    
    public String getWind() { return wind; }
    public void setWind(String wind) { this.wind = wind; }
    
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    
    @Override
    public String toString() {
        return "Weather{id=" + id + ", regionId=" + regionId + ", date=" + date + 
               ", temperature='" + temperature + "', humidity='" + humidity + "'}";
    }
}