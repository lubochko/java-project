package com.example.carsharing1.service;

import com.example.carsharing1.entity.Location;
import com.example.carsharing1.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;
    @InjectMocks
    private LocationService locationService;

    @Test
    void getAllLocationsSortsById() {
        when(locationRepository.findAll()).thenReturn(List.of(location(2L), location(1L)));
        List<Location> result = locationService.getAllLocations();
        assertEquals(List.of(1L, 2L), result.stream().map(Location::getId).toList());
    }

    @Test
    void getLocationByIdReturnsOptional() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location(1L)));
        when(locationRepository.findById(2L)).thenReturn(Optional.empty());
        assertEquals(1L, locationService.getLocationById(1L).orElseThrow().getId());
        assertTrue(locationService.getLocationById(2L).isEmpty());
    }

    @Test
    void getLocationsByCitySortsById() {
        when(locationRepository.findByCity("Moscow")).thenReturn(List.of(location(3L), location(1L)));
        List<Location> result = locationService.getLocationsByCity("Moscow");
        assertEquals(List.of(1L, 3L), result.stream().map(Location::getId).toList());
    }

    private Location location(Long id) {
        Location location = new Location();
        location.setId(id);
        location.setCity("City");
        location.setAddress("Addr");
        return location;
    }
}
