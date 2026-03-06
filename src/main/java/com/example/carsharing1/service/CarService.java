package com.example.carsharing1.service;

import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Feature;
import com.example.carsharing1.entity.Location;
import com.example.carsharing1.enums.CarStatus;
import com.example.carsharing1.mapper.CarMapper;
import com.example.carsharing1.repository.CarRepository;
import com.example.carsharing1.repository.FeatureRepository;
import com.example.carsharing1.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final LocationRepository locationRepository;
    private final FeatureRepository featureRepository;


    public List<CarDto> getAllCars() {
        return carRepository.findAll().stream()
                .map(CarMapper::toDto)
                .collect(Collectors.toList());
    }

    public CarDto getCarById(Long id) {
        return carRepository.findById(id)
                .map(CarMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public CarDto createCar(CarDto carDto) {
        Car car = CarMapper.toEntity(carDto);
        Car savedCar = carRepository.save(car);
        return CarMapper.toDto(savedCar);
    }

    @Transactional
    public CarDto updateCar(Long id, CarDto carDto) {
        return carRepository.findById(id)
                .map(car -> {
                    car.setBrand(carDto.getBrand());
                    car.setModel(carDto.getModel());
                    car.setPricePerMinute(carDto.getPricePerMinute());
                    car.setStatus(carDto.getStatus());
                    car.setLicensePlate(carDto.getLicensePlate());
                    car.setYear(carDto.getYear());
                    car.setFuelLevel(carDto.getFuelLevel());
                    return CarMapper.toDto(carRepository.save(car));
                })
                .orElse(null);
    }

    @Transactional
    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    public List<CarDto> getAllCarsWithNPlusOneProblem() {
        log.info("ДЕМОНСТРАЦИЯ ПРОБЛЕМЫ N+1");
        List<Car> cars = carRepository.findAll();

        List<CarDto> result = new ArrayList<>();

        for (Car car : cars) {

            Location location = car.getLocation();
            Set<Feature> features = car.getFeatures();

            log.info("Машина ID: {}, Локация: {}, Количество особенностей: {}",
                    car.getId(),
                    location != null ? location.getCity() : "не указана",
                    features.size());

            result.add(CarMapper.toDto(car));
        }

        return result;
    }

    public CarDto getCarByIdWithEntityGraph(Long id) {
        log.info("РЕШЕНИЕ ПРОБЛЕМЫ N+1 через @EntityGraph");

        return carRepository.findByIdWithDetails(id)
                .map(CarMapper::toDto)
                .orElse(null);
    }


    public List<CarDto> getAvailableCarsWithFetchJoin() {
        log.info("РЕШЕНИЕ ПРОБЛЕМЫ N+1 через FETCH JOIN");

        List<Car> cars = carRepository.findByStatusWithDetails(CarStatus.AVAILABLE);
        return cars.stream()
                .map(CarMapper::toDto)
                .collect(Collectors.toList());
    }


    public void saveCarWithFeaturesWithoutTransaction(CarDto carDto, List<Long> featureIds, Long locationId) {
        log.info("СОХРАНЕНИЕ БЕЗ @Transactional");

        Car car = CarMapper.toEntity(carDto);

        Location location = locationRepository.findById(locationId).orElse(null);
        if (location == null) {
            log.error("Локация не найдена");
            return;
        }
        car.setLocation(location);
        car.setStatus(CarStatus.AVAILABLE);


        Car savedCar = carRepository.save(car);
        log.info("Машина сохранена с ID: {}", savedCar.getId());


        Set<Feature> featuresToAdd = new HashSet<>();
        for (int i = 0; i < featureIds.size(); i++) {
            Long featureId = featureIds.get(i);
            Feature feature = featureRepository.findById(featureId).orElse(null);
            if (feature == null) {
                log.error("Особенность не найдена: {}", featureId);
                continue;
            }

            featuresToAdd.add(feature);
            log.info("Подготовлена особенность: {}", feature.getName());

            if (i == 2) {
                throw new RuntimeException("ОШИБКА! Проблема при добавлении третьей особенности");
            }
        }

        savedCar.getFeatures().addAll(featuresToAdd);
        carRepository.save(savedCar);
        log.info("Все особенности успешно добавлены");
    }


    @Transactional
    public void saveCarWithFeaturesWithTransaction(CarDto carDto, List<Long> featureIds, Long locationId) {
        log.info("СОХРАНЕНИЕ С @Transactional");

        Car car = CarMapper.toEntity(carDto);

        Location location = locationRepository.findById(locationId).orElse(null);
        if (location == null) {
            log.error("Локация не найдена");
            return;
        }
        car.setLocation(location);
        car.setStatus(CarStatus.AVAILABLE);


        Car savedCar = carRepository.save(car);
        log.info("Машина сохранена с ID: {}", savedCar.getId());


        Set<Feature> featuresToAdd = new HashSet<>();
        for (int i = 0; i < featureIds.size(); i++) {
            Long featureId = featureIds.get(i);
            Feature feature = featureRepository.findById(featureId).orElse(null);
            if (feature == null) {
                log.error("Особенность не найдена: {}", featureId);
                continue;
            }

            featuresToAdd.add(feature);
            log.info("Подготовлена особенность: {}", feature.getName());

            if (i == 2) {
                throw new RuntimeException("ОШИБКА! Транзакция будет откачена");
            }
        }

        savedCar.getFeatures().addAll(featuresToAdd);
        carRepository.save(savedCar);
        log.info("Все особенности успешно добавлены");
    }
}