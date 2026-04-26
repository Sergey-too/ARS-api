package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    @Autowired
    private RegionRepository regionRepository;
        
    @GetMapping
    public List<Region> getAllRegions() {
        return regionRepository.findAllOrdered();
    }

    @PostMapping
    public Region createRegion(@RequestBody Region region) {
        return regionRepository.save(region);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Region> updateRegion(@PathVariable Integer id, @RequestBody Region regionDetails) {
        return regionRepository.findById(id).map(region -> {
            region.setName(regionDetails.getName());
            return ResponseEntity.ok(regionRepository.save(region));
        }).orElse(ResponseEntity.notFound().build());
    }
}