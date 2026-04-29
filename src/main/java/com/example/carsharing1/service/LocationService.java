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

    @Transactional
    public Location createLocation(Location location) {
        location.setId(null);
        return locationRepository.save(location);
    }

    @Transactional
    public Optional<Location> updateLocation(Long id, Location location) {
        return locationRepository.findById(id)
                .map(existingLocation -> {
                    existingLocation.setCity(location.getCity());
                    existingLocation.setAddress(location.getAddress());
                    existingLocation.setLatitude(location.getLatitude());
                    existingLocation.setLongitude(location.getLongitude());
                    existingLocation.setCapacity(location.getCapacity());
                    return locationRepository.save(existingLocation);
                });
    }

    @Transactional
    public boolean deleteLocation(Long id) {
        Optional<Location> locationOptional = locationRepository.findById(id);
        if (locationOptional.isEmpty()) {
            return false;
        }

        Location location = locationOptional.get();
        location.getCars().forEach(car -> car.setLocation(null));
        locationRepository.delete(location);
        return true;
    }
}