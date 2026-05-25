package com.example.backend;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "weather_atm") 
public class Weather {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "region_id")
    private Integer regionId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "temperature_min")
    private Short temperatureMin;    
    
    @Column(name = "temperature_max")
    private Short temperatureMax;     
    
    @Column(name = "humidity_min")
    private Short humidityMin;        
    
    @Column(name = "humidity_max")
    private Short humidityMax;       
    
    @Column(name = "precipitation")
    private Float precipitation;      
    
    @Column(name = "wind_min")
    private Short windMin;            
    
    @Column(name = "wind_max")
    private Short windMax;            
    
    @Column(name = "gusts_of_wind")
    private Short gustsOfWind;
    
    @Column(name = "pressure")
    private Short pressure;            

    public Weather() {}

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Short getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(Short temperatureMin) { this.temperatureMin = temperatureMin; }

    public Short getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(Short temperatureMax) { this.temperatureMax = temperatureMax; }

    public Short getHumidityMin() { return humidityMin; }
    public void setHumidityMin(Short humidityMin) { this.humidityMin = humidityMin; }

    public Short getHumidityMax() { return humidityMax; }
    public void setHumidityMax(Short humidityMax) { this.humidityMax = humidityMax; }

    public Float getPrecipitation() { return precipitation; }
    public void setPrecipitation(Float precipitation) { this.precipitation = precipitation; }

    public Short getWindMin() { return windMin; }
    public void setWindMin(Short windMin) { this.windMin = windMin; }

    public Short getWindMax() { return windMax; }
    public void setWindMax(Short windMax) { this.windMax = windMax; }

    public Short getGustsOfWind() { return gustsOfWind; }
    public void setGustsOfWind(Short gustsOfWind) { this.gustsOfWind = gustsOfWind; }

    public Short getPressure() { return pressure; }
    public void setPressure(Short pressure) { this.pressure = pressure; }
}