package com.example.backend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    
    @Autowired
    private RegionRepository regionRepository;
    
    @Autowired
    private WeatherRepository weatherRepository;
    
    // 1. Получить все регионы
    @GetMapping("/regions")
    public List<Region> getAllRegions() {
        return regionRepository.findAllOrdered();
    }
    
    // 2. Получить погоду для региона (последние 6 дней)
    @GetMapping("/region/{regionId}")
    public Map<String, Object> getWeatherByRegion(@PathVariable Integer regionId) {
        Map<String, Object> response = new HashMap<>();
        
        Region region = regionRepository.findById(regionId).orElse(null);
        if (region == null) {
            response.put("error", "Регион не найден");
            return response;
        }
        
        LocalDate startDate = LocalDate.now().minusDays(6);
        List<Weather> weatherData = weatherRepository.findByRegionIdAndDateAfter(regionId, startDate);
        
        response.put("region", region);
        response.put("weather", weatherData);
        response.put("count", weatherData.size());
        
        return response;
    }
    
    // 3. Получить погоду по имени региона (для Android)
    @GetMapping("/android/{regionName}")
    public Map<String, Object> getWeatherForAndroid(@PathVariable String regionName) {
        Map<String, Object> response = new HashMap<>();
        
        Region region = regionRepository.findByName(regionName);
        if (region == null) {
            // Если регион не найден, создаем его и возвращаем тестовые данные
            region = new Region(regionName);
            region = regionRepository.save(region);
            
            response.put("region", region.getName());
            response.put("weather", generateTestWeather(region.getId()));
            response.put("isTestData", true);
            response.put("message", "Регион создан, данные тестовые");
        } else {
            // Получаем реальные данные
            LocalDate startDate = LocalDate.now().minusDays(6);
            List<Weather> weatherData = weatherRepository.findByRegionIdAndDateAfter(region.getId(), startDate);
            
            if (weatherData.isEmpty()) {
                // Если данных нет, создаем тестовые
                weatherData = generateTestWeather(region.getId());
                weatherRepository.saveAll(weatherData);
                response.put("isTestData", true);
                response.put("message", "Данные отсутствовали, созданы тестовые");
            } else {
                response.put("isTestData", false);
                response.put("message", "Реальные данные из БД");
            }
            
            response.put("region", region.getName());
            response.put("weather", convertToAndroidFormat(weatherData));
        }
        
        return response;
    }
    
    // 4. Добавить данные о погоде
    @PostMapping("/add")
    public Map<String, Object> addWeatherData(@RequestBody WeatherRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Weather weather = new Weather();
            weather.setRegionId(request.getRegionId());
            weather.setDate(request.getDate());
            weather.setTemperature(request.getTemperature());
            weather.setHumidity(request.getHumidity());
            weather.setPrecipitation(request.getPrecipitation());
            weather.setWind(request.getWind());
            weather.setCondition(request.getCondition());
            
            weatherRepository.save(weather);
            
            response.put("status", "success");
            response.put("message", "Данные о погоде сохранены");
            response.put("id", weather.getId());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    //5. Получить ВСЕ данные по региону для админки
    @GetMapping("/admin/all/{regionName}")
    public Map<String, Object> getAllWeatherForRegion(@PathVariable String regionName) {
        Map<String, Object> response = new HashMap<>();
        
        Region region = regionRepository.findByName(regionName);
        if (region == null) {
            response.put("error", "Регион не найден");
            return response;
        }
        
        // Получаем ВСЕ данные по региону (без ограничения в 6 дней)
        List<Weather> weatherData = weatherRepository.findByRegionId(region.getId());
        
        response.put("region", region.getName());
        response.put("weather", convertToAndroidFormat(weatherData));
        response.put("count", weatherData.size());
        response.put("isTestData", false);
        response.put("message", "Все данные по региону");
        
        return response;
    }

    // Удалить данные по дате и региону
    @DeleteMapping("/delete-by-date-region")
    public Map<String, Object> deleteWeatherByDateAndRegion(
            @RequestParam String date,
            @RequestParam String regionName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Находим регион
            Region region = regionRepository.findByName(regionName);
            if (region == null) {
                response.put("success", false);
                response.put("error", "Регион не найден");
                return response;
            }
            
            // Парсим дату
            LocalDate localDate = LocalDate.parse(date);
            
            // Находим и удаляем запись
            Weather weather = weatherRepository.findByRegionIdAndDate(region.getId(), localDate);
            if (weather == null) {
                response.put("success", false);
                response.put("error", "Данные за указанную дату не найдены");
                return response;
            }
            
            weatherRepository.delete(weather);
            
            response.put("success", true);
            response.put("message", "Данные успешно удалены");
            response.put("deletedId", weather.getId());
            response.put("date", date);
            response.put("region", regionName);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка удаления: " + e.getMessage());
        }
        
        return response;
    }
    // Вспомогательные методы
    
    private List<Weather> generateTestWeather(Integer regionId) {
        List<Weather> testData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        String[] conditions = {"Ясно", "Облачно", "Пасмурно", "Дождь", "Снег"};
        Random random = new Random();
        
        for (int i = 0; i < 6; i++) {
            LocalDate date = today.minusDays(i);
            String temp = String.format("%d°C", -10 + random.nextInt(15));
            String humidity = String.format("%d%%", 60 + random.nextInt(30));
            String precipitation = String.format("%.1f мм", random.nextDouble() * 5);
            String wind = String.format("%.1f м/с", 1 + random.nextDouble() * 10);
            String condition = conditions[random.nextInt(conditions.length)];
            
            Weather weather = new Weather(
                regionId, date, temp, humidity, precipitation, wind, condition
            );
            testData.add(weather);
        }
        
        return testData;
    }
    
    private List<Map<String, String>> convertToAndroidFormat(List<Weather> weatherList) {
        List<Map<String, String>> result = new ArrayList<>();
        
        for (Weather weather : weatherList) {
            Map<String, String> map = new HashMap<>();
            map.put("date", weather.getDate().toString());
            map.put("temperature", weather.getTemperature());
            map.put("wind", weather.getWind());
            map.put("pressure", "1015"); // Добавьте реальное поле если нужно
            map.put("humidity", weather.getHumidity());
            map.put("precipitation", weather.getPrecipitation());
            result.add(map);
        }
        
        return result;
    }
    
    // Класс для запроса
    public static class WeatherRequest {
        private Integer regionId;
        private LocalDate date;
        private String temperature;
        private String humidity;
        private String precipitation;
        private String wind;
        private String condition;
        
        // Геттеры и сеттеры
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
    }
}