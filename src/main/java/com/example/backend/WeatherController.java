package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

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
}