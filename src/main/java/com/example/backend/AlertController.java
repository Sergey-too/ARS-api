package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    
    @Autowired 
    private WeatherAlertRepository alertRepository;
    
    @Autowired 
    private AreaRepository areaRepository;

    @GetMapping("/check/{userId}")
    public List<WeatherAlert> getAlertsForUser(@PathVariable Integer userId) {
        List<Integer> regionIds = areaRepository.findByUserId(userId).stream()
                .map(Area::getRegionId)
                .distinct()
                .toList();

        if (regionIds.isEmpty()) return Collections.emptyList();

        return alertRepository.findActiveAlertsForRegions(regionIds);
    }
}