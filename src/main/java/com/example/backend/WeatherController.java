package com.example.backend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    
    @GetMapping("/region/{regionId}")
    public ResponseEntity<Map<String, Object>> getWeatherByRegionId(@PathVariable Integer regionId) {
        Map<String, Object> response = new HashMap<>();
        
        Region region = regionRepository.findById(regionId).orElse(null);
        if (region == null) {
            response.put("error", "Регион не найден");
            return ResponseEntity.notFound().build();
        }

        LocalDate today = LocalDate.now();
        List<Weather> weatherData = weatherRepository.findByRegionIdAndDateAfter(regionId, today);
        
        response.put("region", region.getName());
        response.put("isTestData", false);

        if (weatherData.isEmpty()) {
            response.put("message", "На сегодня и будущие даты данных нет");
            response.put("weather", new ArrayList<>());
        } else {
            response.put("message", "Прогноз загружен");
            response.put("weather", convertToAndroidFormat(weatherData));
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllWeather() {
        List<Weather> allWeather = weatherRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Weather w : allWeather) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", w.getId());
            map.put("regionId", w.getRegionId());
            map.put("date", w.getDate().toString());
            map.put("temperatureMin", w.getTemperatureMin());
            map.put("temperatureMax", w.getTemperatureMax());
            map.put("humidityMin", w.getHumidityMin());
            map.put("humidityMax", w.getHumidityMax());
            map.put("precipitation", w.getPrecipitation());
            map.put("windMin", w.getWindMin());
            map.put("windMax", w.getWindMax());
            map.put("gustsOfWind", w.getGustsOfWind());
            map.put("pressure", w.getPressure());
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteWeather(@PathVariable Integer id) {
        if (weatherRepository.existsById(id)) {
            weatherRepository.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Запись удалена");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Запись не найдена");
            return ResponseEntity.notFound().build();
        }
    }

    private List<Map<String, Object>> convertToAndroidFormat(List<Weather> weatherList) {
    List<Map<String, Object>> result = new ArrayList<>();
    
    for (Weather w : weatherList) {
        Map<String, Object> map = new HashMap<>();

        map.put("date", w.getDate().toString());
        map.put("pressure", w.getPressure() != null ? w.getPressure().toString() : "0");

        map.put("temperatureMin", w.getTemperatureMin() != null ? w.getTemperatureMin().doubleValue() : 0.0);
        map.put("temperatureMax", w.getTemperatureMax() != null ? w.getTemperatureMax().doubleValue() : 0.0);
        
        map.put("humidityMin", w.getHumidityMin() != null ? w.getHumidityMin().doubleValue() : 0.0);
        map.put("humidityMax", w.getHumidityMax() != null ? w.getHumidityMax().doubleValue() : 0.0);
        
        map.put("windMin", w.getWindMin() != null ? w.getWindMin().doubleValue() : 0.0);
        map.put("windMax", w.getWindMax() != null ? w.getWindMax().doubleValue() : 0.0);
        
        map.put("precipitation", w.getPrecipitation() != null ? w.getPrecipitation().doubleValue() : 0.0);

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

    @GetMapping("/by-date/{regionId}/{date}")
public ResponseEntity<WeatherData> getWeatherByDate(@PathVariable Integer regionId, @PathVariable String date) {
    try {
        LocalDate localDate = LocalDate.parse(date);
        Weather weather = weatherRepository.findByRegionIdAndDate(regionId, localDate);
        
        if (weather == null) {
            return ResponseEntity.notFound().build();
        }
        
        WeatherData data = new WeatherData();
        data.setDate(weather.getDate().toString());
        data.setTemperatureMin(weather.getTemperatureMin() != null ? weather.getTemperatureMin().toString() : null);
        data.setTemperatureMax(weather.getTemperatureMax() != null ? weather.getTemperatureMax().toString() : null);
        data.setHumidityMin(weather.getHumidityMin() != null ? weather.getHumidityMin().toString() : null);
        data.setHumidityMax(weather.getHumidityMax() != null ? weather.getHumidityMax().toString() : null);
        data.setPrecipitation(weather.getPrecipitation() != null ? weather.getPrecipitation().toString() : null);
        data.setWindMin(weather.getWindMin() != null ? weather.getWindMin().toString() : null);
        data.setWindMax(weather.getWindMax() != null ? weather.getWindMax().toString() : null);
        data.setPressure(weather.getPressure() != null ? weather.getPressure().toString() : null);
        
        return ResponseEntity.ok(data);
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
}