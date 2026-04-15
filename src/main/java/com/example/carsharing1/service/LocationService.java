package com.example.carsharing1.service;

import com.example.carsharing1.entity.Location;
import com.example.carsharing1.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private static final Comparator<Location> BY_ID = Comparator.comparing(Location::getId);

    @Transactional(readOnly = true)
    public List<Location> getAllLocations() {
        log.info("Получение списка всех локаций");
        return locationRepository.findAll().stream()
                .sorted(BY_ID)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Location> getLocationById(Long id) {
        log.info("Получение локации с ID: {}", id);
        return locationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Location> getLocationsByCity(String city) {
        log.info("Получение локаций в городе: {}", city);
        return locationRepository.findByCity(city).stream()
                .sorted(BY_ID)
                .toList();
    }
}