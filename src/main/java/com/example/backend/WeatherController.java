package com.example.backend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private RegionRepository regionRepository;
    
    @Autowired
    private WeatherRepository weatherRepository;

    @GetMapping("/regions")
    public List<Region> getAllRegions() {
        return regionRepository.findAllOrdered();
    }

    @GetMapping("/android/{regionName}")
    public Map<String, Object> getWeatherForAndroid(@PathVariable String regionName) {
        Map<String, Object> response = new HashMap<>();
        Region region = regionRepository.findByName(regionName);
        
        if (region == null) {
            response.put("error", "Регион не найден");
            return response;
        }

        LocalDate today = LocalDate.now();
        List<Weather> weatherData = weatherRepository.findByRegionIdAndDateAfter(region.getId(), today);
        
        response.put("region", region.getName());
        response.put("isTestData", false);

        if (weatherData.isEmpty()) {
            response.put("message", "На сегодня и будущие даты данных нет");
            response.put("weather", new ArrayList<>());
        } else {
            response.put("message", "Прогноз загружен");
            response.put("weather", convertToAndroidFormat(weatherData));
        }
        
        return response;
    }

    private List<Map<String, Object>> convertToAndroidFormat(List<Weather> weatherList) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Weather w : weatherList) {
            Map<String, Object> map = new HashMap<>();

            map.put("date", w.getDate().toString());
            map.put("pressure", w.getPressure());

            map.put("temperatureMin", parseDoubleSafe(w.getTemperatureMin()));
            map.put("temperatureMax", parseDoubleSafe(w.getTemperatureMax()));
            
            map.put("humidityMin", parseDoubleSafe(w.getHumidityMin()));
            map.put("humidityMax", parseDoubleSafe(w.getHumidityMax()));
            
            map.put("windMin", parseDoubleSafe(w.getWindMin()));
            map.put("windMax", parseDoubleSafe(w.getWindMax()));
            
            map.put("precipitation", parseDoubleSafe(w.getPrecipitation()));

            result.add(map);
        }
        return result;
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            String clean = value.replaceAll("[^0-9.\\-]", "");
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    @GetMapping("/compare/{regionId}")
    public List<WeatherComparisonDTO> getComparison(@PathVariable Long regionId) {
        List<Object[]> results = weatherRepository.getYearlyComparisonRaw(regionId);
        List<WeatherComparisonDTO> dtos = new ArrayList<>();

        if (results == null || results.isEmpty()) {
            return dtos;
        }

        for (Object[] row : results) {
            WeatherComparisonDTO dto = new WeatherComparisonDTO();
            
            dto.setMonthName(row[0] != null ? row[0].toString() : "Unknown");
            dto.setAvgFactTemp(row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
            dto.setNormalTemp(row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
            dto.setAvgFactHumidity(row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
            dto.setNormalHumidity(row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);
            
            dtos.add(dto);
        }

        return dtos; 
    }
    private Double safeDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
    
