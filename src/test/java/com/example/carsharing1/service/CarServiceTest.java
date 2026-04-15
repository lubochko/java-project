package com.example.carsharing1.service;

import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Feature;
import com.example.carsharing1.entity.Location;
import com.example.carsharing1.enums.BookingStatus;
import com.example.carsharing1.exception.CarServiceException;
import com.example.carsharing1.exception.FeatureNotFoundException;
import com.example.carsharing1.exception.LocationNotFoundException;
import com.example.carsharing1.repository.CarRepository;
import com.example.carsharing1.repository.FeatureRepository;
import com.example.carsharing1.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private FeatureRepository featureRepository;
    @InjectMocks
    private CarService carService;

    @Test
    void getAllCarsSortsById() {
        when(carRepository.findAll()).thenReturn(List.of(car(2L), car(1L)));
        List<CarDto> result = carService.getAllCars();
        assertEquals(List.of(1L, 2L), result.stream().map(CarDto::getId).toList());
    }

    @Test
    void getAllActiveAndAvailableSortById() {
        when(carRepository.findAllActive()).thenReturn(List.of(car(3L), car(1L)));
        when(carRepository.findAvailableCars()).thenReturn(List.of(car(4L), car(2L)));
        assertEquals(List.of(1L, 3L), carService.getAllActiveCars().stream().map(CarDto::getId).toList());
        assertEquals(List.of(2L, 4L), carService.getAvailableCars().stream().map(CarDto::getId).toList());
    }

    @Test
    void getCarByIdReturnsOptional() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(car(1L)));
        when(carRepository.findById(2L)).thenReturn(Optional.empty());
        assertTrue(carService.getCarById(1L).isPresent());
        assertTrue(carService.getCarById(2L).isEmpty());
    }

    @Test
    void createCarSetsActiveAndSaves() {
        CarDto dto = carDto(null);
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        CarDto created = carService.createCar(dto);
        assertEquals(1L, created.getId());
        assertTrue(created.isActive());
    }

    @Test
    void createCarWithLocationSuccessAndNotFound() {
        CarDto dto = carDto(null);
        Location location = location(10L);
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            c.setId(11L);
            return c;
        });
        CarDto created = carService.createCarWithLocation(dto, 10L);
        assertEquals(11L, created.getId());
        assertEquals("City", created.getLocationCity());

        when(locationRepository.findById(999L)).thenReturn(Optional.empty());
        Long missingLocationId = 999L;
        assertThrows(LocationNotFoundException.class,
                () -> carService.createCarWithLocation(dto, missingLocationId));
    }

    @Test
    void updateCarSuccessAndNotFound() {
        Car existing = car(1L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
        CarDto patch = new CarDto();
        patch.setBrand("NewBrand");
        patch.setModel("NewModel");
        patch.setPricePerMinute(2.0);
        patch.setLicensePlate("XYZ-11");
        patch.setYear(2024);
        patch.setFuelLevel(70.0);
        CarDto updated = carService.updateCar(1L, patch).orElseThrow();
        assertEquals("NewBrand", updated.getBrand());
        assertEquals("NewModel", updated.getModel());
        assertEquals("XYZ-11", updated.getLicensePlate());

        when(carRepository.findById(2L)).thenReturn(Optional.empty());
        assertTrue(carService.updateCar(2L, patch).isEmpty());
    }

    @Test
    void updateCarWithNullFieldsKeepsOldValues() {
        Car existing = car(5L);
        when(carRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        CarDto patch = new CarDto();
        CarDto updated = carService.updateCar(5L, patch).orElseThrow();

        assertEquals("Brand", updated.getBrand());
        assertEquals("Model", updated.getModel());
        assertEquals("ABC-5", updated.getLicensePlate());
    }

    @Test
    void updateCarStatusSuccessAndNotFound() {
        Car existing = car(1L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
        CarDto updated = carService.updateCarStatus(1L, false).orElseThrow();
        assertTrue(!updated.isActive());

        when(carRepository.findById(3L)).thenReturn(Optional.empty());
        assertTrue(carService.updateCarStatus(3L, true).isEmpty());
    }

    @Test
    void deleteCarNotFoundActiveBookingAndSuccess() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());
        carService.deleteCar(1L);
        verify(carRepository, never()).delete(any(Car.class));

        Car withActive = car(2L);
        Booking active = new Booking();
        active.setStatus(BookingStatus.ACTIVE);
        withActive.getBookings().add(active);
        when(carRepository.findById(2L)).thenReturn(Optional.of(withActive));
        Long carIdWithActiveBooking = 2L;
        assertThrows(CarServiceException.class, () -> carService.deleteCar(carIdWithActiveBooking));

        Car normal = car(3L);
        Booking completed = new Booking();
        completed.setStatus(BookingStatus.COMPLETED);
        completed.setCar(normal);
        normal.getBookings().add(completed);
        when(carRepository.findById(3L)).thenReturn(Optional.of(normal));
        carService.deleteCar(3L);
        verify(carRepository).delete(normal);
        assertEquals(0, normal.getBookings().size());
    }

    @Test
    void complexCriteriaAndNativeAreSorted() {
        when(carRepository.findCarsByComplexCriteria("a", "f")).thenReturn(List.of(car(2L), car(1L)));
        when(carRepository.findCarsByComplexCriteriaNative("a", "f")).thenReturn(List.of(car(3L), car(1L)));
        assertEquals(List.of(1L, 2L), carService.findCarsByComplexCriteria("a", "f").stream().map(CarDto::getId).toList());
        assertEquals(List.of(1L, 3L), carService.findCarsByComplexCriteriaNative("a", "f").stream().map(CarDto::getId).toList());
    }

    @Test
    void complexCriteriaPagedMapsPage() {
        Page<Car> page = new PageImpl<>(List.of(car(1L), car(2L)));
        when(carRepository.findCarsByComplexCriteriaPaged(any(), any(), any())).thenReturn(page);
        Page<CarDto> result = carService.findCarsByComplexCriteriaPaged("a", "f", 0, 10, "id", "ASC");
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void cacheMissThenHit() {
        when(carRepository.findCarsByComplexCriteria("e", "feat")).thenReturn(List.of(car(1L)));
        List<CarDto> first = carService.findCarsWithCache("e", "feat", 0, 10, "id", "ASC");
        List<CarDto> second = carService.findCarsWithCache("e", "feat", 0, 10, "id", "ASC");
        assertEquals(1, first.size());
        assertEquals(1, second.size());
        verify(carRepository, times(1)).findCarsByComplexCriteria("e", "feat");
    }

    @Test
    void cacheIsInvalidatedAfterMutations() {
        when(carRepository.findCarsByComplexCriteria("e", "feat")).thenReturn(List.of(car(1L)));
        carService.findCarsWithCache("e", "feat", 0, 10, "id", "ASC");
        carService.findCarsWithCache("e", "feat", 0, 10, "id", "ASC");
        verify(carRepository, times(1)).findCarsByComplexCriteria("e", "feat");

        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(99L);
            }
            return c;
        });
        carService.createCar(carDto(null));

        carService.findCarsWithCache("e", "feat", 0, 10, "id", "ASC");
        verify(carRepository, times(2)).findCarsByComplexCriteria("e", "feat");
    }

    @Test
    void nPlusOneDemoAndEntityGraphAndFetchJoin() {
        Car c1 = car(1L);
        c1.setLocation(location(1L));
        Feature f = feature(1L, "GPS");
        c1.setFeatures(Set.of(f));
        when(carRepository.findAll()).thenReturn(List.of(c1));
        assertEquals(1, carService.getAllCarsWithNPlusOneProblem().size());

        when(carRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(c1));
        when(carRepository.findByIdWithDetails(2L)).thenReturn(Optional.empty());
        assertNotNull(carService.getCarByIdWithEntityGraph(1L).orElse(null));
        assertTrue(carService.getCarByIdWithEntityGraph(2L).isEmpty());

        when(carRepository.findAllActiveWithDetails()).thenReturn(List.of(car(2L), car(1L)));
        assertEquals(List.of(1L, 2L), carService.getAllActiveCarsWithFetchJoin().stream().map(CarDto::getId).toList());
    }

    @Test
    void nPlusOneDemoHandlesEmptyAndNullLocation() {
        when(carRepository.findAll()).thenReturn(List.of(car(1L), car(2L)));
        carService.getAllCarsWithNPlusOneProblem();

        Car noLocation = car(9L);
        noLocation.setLocation(null);
        when(carRepository.findAll()).thenReturn(List.of(noLocation));
        assertEquals(1, carService.getAllCarsWithNPlusOneProblem().size());
    }

    @Test
    void saveCarWithFeaturesWithoutTransactionThrowsOnThirdFeature() {
        CarDto dto = carDto(null);
        List<Long> featureIds = List.of(1L, 2L, 3L);
        Long locationId = 1L;
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location(1L)));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(100L);
            }
            return c;
        });
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature(1L, "A")));
        when(featureRepository.findById(2L)).thenReturn(Optional.of(feature(2L, "B")));
        when(featureRepository.findById(3L)).thenReturn(Optional.of(feature(3L, "C")));

        assertThrows(CarServiceException.class,
                () -> carService.saveCarWithFeaturesWithoutTransaction(dto, featureIds, locationId));
    }

    @Test
    void saveCarWithFeaturesWithoutTransactionSuccess() {
        CarDto dto = carDto(null);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location(1L)));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(100L);
            }
            return c;
        });
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature(1L, "A")));
        when(featureRepository.findById(2L)).thenReturn(Optional.of(feature(2L, "B")));

        carService.saveCarWithFeaturesWithoutTransaction(dto, List.of(1L, 2L), 1L);

        verify(carRepository, times(2)).save(any(Car.class));
    }

    @Test
    void saveCarWithFeaturesWithTransactionThrowsOnThirdFeature() {
        CarDto dto = carDto(null);
        List<Long> featureIds = List.of(1L, 2L, 3L);
        Long locationId = 1L;
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location(1L)));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(100L);
            }
            return c;
        });
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature(1L, "A")));
        when(featureRepository.findById(2L)).thenReturn(Optional.of(feature(2L, "B")));
        when(featureRepository.findById(3L)).thenReturn(Optional.of(feature(3L, "C")));

        assertThrows(CarServiceException.class,
                () -> carService.saveCarWithFeaturesWithTransaction(dto, featureIds, locationId));
    }

    @Test
    void saveCarWithFeaturesWithTransactionSuccess() {
        CarDto dto = carDto(null);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location(1L)));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(100L);
            }
            return c;
        });
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature(1L, "A")));
        when(featureRepository.findById(2L)).thenReturn(Optional.of(feature(2L, "B")));

        carService.saveCarWithFeaturesWithTransaction(dto, List.of(1L, 2L), 1L);

        verify(carRepository, times(2)).save(any(Car.class));
    }

    @Test
    void saveCarWithFeaturesThrowsWhenFeatureMissing() {
        CarDto dto = carDto(null);
        List<Long> featureIds = List.of(1L);
        Long locationId = 1L;
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location(1L)));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(100L);
            }
            return c;
        });
        when(featureRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(FeatureNotFoundException.class,
                () -> carService.saveCarWithFeaturesWithoutTransaction(dto, featureIds, locationId));
    }

    @Test
    void saveCarWithFeaturesWithTransactionThrowsWhenFeatureMissing() {
        CarDto dto = carDto(null);
        List<Long> featureIds = List.of(1L);
        Long locationId = 1L;
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location(1L)));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(100L);
            }
            return c;
        });
        when(featureRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FeatureNotFoundException.class,
                () -> carService.saveCarWithFeaturesWithTransaction(dto, featureIds, locationId));
    }

    private Car car(Long id) {
        Car car = new Car();
        car.setId(id);
        car.setBrand("Brand");
        car.setModel("Model");
        car.setPricePerMinute(1.2);
        car.setLicensePlate("ABC-" + id);
        car.setYear(2020);
        car.setFuelLevel(80.0);
        car.setActive(true);
        return car;
    }

    private CarDto carDto(Long id) {
        CarDto dto = new CarDto();
        dto.setId(id);
        dto.setBrand("Brand");
        dto.setModel("Model");
        dto.setPricePerMinute(1.2);
        dto.setLicensePlate("ABC-1");
        dto.setYear(2020);
        dto.setFuelLevel(50.0);
        dto.setActive(true);
        return dto;
    }

    private Location location(Long id) {
        Location location = new Location();
        location.setId(id);
        location.setCity("City");
        location.setAddress("Addr");
        return location;
    }

    private Feature feature(Long id, String name) {
        Feature feature = new Feature();
        feature.setId(id);
        feature.setName(name);
        return feature;
    }
}
