package com.example.carsharing1.controller;

import com.example.carsharing1.dto.LocationDto;
import com.example.carsharing1.entity.Location;
import com.example.carsharing1.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        log.info("GET /api/locations - запрос всех локаций");
        List<LocationDto> locations = locationService.getAllLocations().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long id) {
        log.info("GET /api/locations/{} - запрос локации по ID", id);
        return locationService.getLocationById(id)
                .map(location -> ResponseEntity.ok(toDto(location)))
                .orElseGet(() -> {
                    log.warn("Локация с ID {} не найдена", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/city")
    public ResponseEntity<List<LocationDto>> getLocationsByCity(@RequestParam String city) {
        log.info("GET /api/locations/city?city={} - запрос локаций по городу", city);
        List<LocationDto> locations = locationService.getLocationsByCity(city).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(locations);
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto location) {
        return ResponseEntity.status(201).body(toDto(locationService.createLocation(toEntity(location))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable Long id,
            @RequestBody LocationDto location) {
        return locationService.updateLocation(id, toEntity(location))
                .map(updatedLocation -> ResponseEntity.ok(toDto(updatedLocation)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        return locationService.deleteLocation(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private LocationDto toDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getCity(),
                location.getAddress(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCapacity());
    }

    private Location toEntity(LocationDto dto) {
        Location location = new Location();
        location.setId(dto.getId());
        location.setCity(dto.getCity());
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setCapacity(dto.getCapacity());
        return location;
    }
}