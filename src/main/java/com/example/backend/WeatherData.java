package com.example.backend;

public class WeatherData {
    private String date;
    private String temperatureMin;
    private String temperatureMax;
    private String humidityMin;
    private String humidityMax;
    private String precipitation;
    private String windMin;
    private String windMax;
    private String pressure;
    
    public WeatherData() {}
    
    // Геттеры
    public String getDate() { return date; }
    public String getTemperatureMin() { return temperatureMin; }
    public String getTemperatureMax() { return temperatureMax; }
    public String getHumidityMin() { return humidityMin; }
    public String getHumidityMax() { return humidityMax; }
    public String getPrecipitation() { return precipitation; }
    public String getWindMin() { return windMin; }
    public String getWindMax() { return windMax; }
    public String getPressure() { return pressure; }
    
    // Сеттеры
    public void setDate(String date) { this.date = date; }
    public void setTemperatureMin(String temperatureMin) { this.temperatureMin = temperatureMin; }
    public void setTemperatureMax(String temperatureMax) { this.temperatureMax = temperatureMax; }
    public void setHumidityMin(String humidityMin) { this.humidityMin = humidityMin; }
    public void setHumidityMax(String humidityMax) { this.humidityMax = humidityMax; }
    public void setPrecipitation(String precipitation) { this.precipitation = precipitation; }
    public void setWindMin(String windMin) { this.windMin = windMin; }
    public void setWindMax(String windMax) { this.windMax = windMax; }
    public void setPressure(String pressure) { this.pressure = pressure; }
}