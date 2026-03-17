package com.example.carsharing1.controller;

import com.example.carsharing1.entity.Location;
import com.example.carsharing1.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        log.info("GET /api/locations - запрос всех локаций");
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        log.info("GET /api/locations/{} - запрос локации по ID", id);
        Location location = locationService.getLocationById(id);

        if (location == null) {
            log.warn("Локация с ID {} не найдена", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(location);
    }

    @GetMapping("/city")
    public ResponseEntity<List<Location>> getLocationsByCity(@RequestParam String city) {
        log.info("GET /api/locations/city?city={} - запрос локаций по городу", city);
        List<Location> locations = locationService.getLocationsByCity(city);
        return ResponseEntity.ok(locations);
    }
}