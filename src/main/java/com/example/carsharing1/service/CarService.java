package com.example.carsharing1.service;

import com.example.carsharing1.cache.CarSearchKey;
import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Feature;
import com.example.carsharing1.entity.Location;
import com.example.carsharing1.exception.CarServiceException;
import com.example.carsharing1.exception.FeatureNotFoundException;
import com.example.carsharing1.exception.LocationNotFoundException;
import com.example.carsharing1.mapper.CarMapper;
import com.example.carsharing1.repository.CarRepository;
import com.example.carsharing1.repository.FeatureRepository;
import com.example.carsharing1.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final LocationRepository locationRepository;
    private final FeatureRepository featureRepository;

    private final Map<CarSearchKey, List<CarDto>> searchCache = new HashMap<>();
    private static final Comparator<CarDto> BY_ID = Comparator.comparing(CarDto::getId);

    private static final String CAR_NOT_FOUND = "Машина с ID {} не найдена";
    private static final String LOCATION_NOT_FOUND = "Локация с ID {} не найдена";
    private static final String FEATURE_NOT_FOUND = "Особенность с ID {} не найдена";
    private static final String CAR_CREATED = "Машина создана с ID: {}";
    private static final String CAR_UPDATED = "Машина с ID {} обновлена";
    private static final String CAR_STATUS_UPDATED = "Статус машины с ID {} обновлен";
    private static final String CAR_DELETED = "Машина с ID {} удалена";
    private static final String CAR_SAVED = "Машина сохранена с ID: {}";
    private static final String FEATURE_PREPARED = "Подготовлена особенность: {}";
    private static final String FEATURES_ADDED = "Добавлено {} особенностей к машине ID: {}";
    private static final String GET_ALL_CARS = "Получение всех машин";
    private static final String GET_ACTIVE_CARS = "Получение всех активных машин";
    private static final String GET_AVAILABLE_CARS = "Получение доступных машин (без активных бронирований)";
    private static final String GET_CAR_BY_ID = "Получение машины с ID: {}";
    private static final String CREATE_CAR = "Создание новой машины";
    private static final String CREATE_CAR_WITH_LOCATION = "Создание новой машины с локацией ID: {}";
    private static final String UPDATE_CAR = "Обновление машины с ID: {}";
    private static final String UPDATE_CAR_STATUS = "Обновление статуса машины ID: {}, active: {}";
    private static final String DELETE_CAR = "Удаление машины с ID: {}";
    private static final String N_PLUS_ONE_DEMO = "ДЕМОНСТРАЦИЯ ПРОБЛЕМЫ N+1";
    private static final String ENTITY_GRAPH_SOLUTION = "РЕШЕНИЕ ПРОБЛЕМЫ N+1 через @EntityGraph";
    private static final String FETCH_JOIN_SOLUTION = "РЕШЕНИЕ ПРОБЛЕМЫ N+1 через FETCH JOIN";
    private static final String WITHOUT_TX_DEMO = "СОХРАНЕНИЕ БЕЗ @Transactional - ДЕМОНСТРАЦИЯ ЧАСТИЧНОГО СОХРАНЕНИЯ";
    private static final String WITH_TX_DEMO = "СОХРАНЕНИЕ С @Transactional - ДЕМОНСТРАЦИЯ ПОЛНОГО ОТКАТА";
    private static final String FEATURES_ADDED_SUCCESS = "Все особенности успешно добавлены (до момента ошибки)";
    private static final String TX_COMMITTED = "Все особенности успешно добавлены (транзакция зафиксирована)";
    private static final String ERROR_ON_THIRD_FEATURE = "ОШИБКА! Проблема при добавлении третьей особенности";
    private static final String ERROR_TX_ROLLBACK = "ОШИБКА! Транзакция будет откачена";
    private static final String CAR_INFO_LOG = "Машина ID: {}, Локация: {}, Количество особенностей: {}, Доступна: {}";
    private static final String ERROR_ACTIVE_BOOKINGS = "Невозможно удалить машину с " +
            "ID {} - есть активные бронирования";


    private static final String SEARCH_WITH_CACHE = "Поиск машин с использованием кэша. Ключ: {}";
    private static final String CACHE_HIT = "КЭШ HIT: данные найдены в кэше. Ключ: {}";
    private static final String CACHE_MISS = "КЭШ MISS: данных нет в кэше. Выполняем запрос к БД. Ключ: {}";
    private static final String CACHE_INVALIDATED = "Инвалидация кэша при изменении данных";
    private static final String SEARCH_JPQL = "Сложный JPQL запрос: email={}, featureName={}";
    private static final String SEARCH_NATIVE = "Native SQL запрос: email={}, featureName={}";
    private static final String SEARCH_PAGED = "Поиск с пагинацией: страница={}, размер={}, сортировка={} {}";

    public List<CarDto> getAllCars() {
        log.info(GET_ALL_CARS);
        return carRepository.findAll().stream()
                .map(CarMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public List<CarDto> getAllActiveCars() {
        log.info(GET_ACTIVE_CARS);
        return carRepository.findAllActive().stream()
                .map(CarMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public List<CarDto> getAvailableCars() {
        log.info(GET_AVAILABLE_CARS);
        return carRepository.findAvailableCars().stream()
                .map(CarMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public Optional<CarDto> getCarById(Long id) {
        log.info(GET_CAR_BY_ID, id);
        return carRepository.findById(id)
                .map(CarMapper::toDto);
    }

    @Transactional
    public CarDto createCar(CarDto carDto) {
        log.info(CREATE_CAR);
        Car car = CarMapper.toEntity(carDto);
        car.setActive(true);
        Car savedCar = carRepository.save(car);
        log.info(CAR_CREATED, savedCar.getId());

        invalidateCache();

        return CarMapper.toDto(savedCar);
    }

    @Transactional
    public CarDto createCarWithLocation(CarDto carDto, Long locationId) {
        log.info(CREATE_CAR_WITH_LOCATION, locationId);
        Car car = prepareCar(carDto, locationId);
        Car savedCar = carRepository.save(car);
        log.info(CAR_CREATED, savedCar.getId());

        invalidateCache();

        return CarMapper.toDto(savedCar);
    }

    @Transactional
    public Optional<CarDto> updateCar(Long id, CarDto carDto) {
        log.info(UPDATE_CAR, id);

        return carRepository.findById(id)
                .map(car -> {
                    updateCarFields(car, carDto);
                    Car updatedCar = carRepository.save(car);
                    log.info(CAR_UPDATED, id);

                    invalidateCache();

                    return CarMapper.toDto(updatedCar);
                });
    }

    @Transactional
    public Optional<CarDto> updateCarStatus(Long id, boolean active) {
        log.info(UPDATE_CAR_STATUS, id, active);

        return carRepository.findById(id)
                .map(car -> {
                    car.setActive(active);
                    Car updatedCar = carRepository.save(car);
                    log.info(CAR_STATUS_UPDATED, id);

                    invalidateCache();

                    return CarMapper.toDto(updatedCar);
                });
    }

    @Transactional
    public void deleteCar(Long id) {
        log.info(DELETE_CAR, id);

        Optional<Car> carOptional = carRepository.findById(id);
        if (carOptional.isEmpty()) {
            log.warn(CAR_NOT_FOUND, id);
            return;
        }
        Car car = carOptional.get();

        boolean hasActiveBookings = car.getBookings().stream()
                .anyMatch(booking -> booking.getStatus() == com.example.carsharing1.enums.BookingStatus.ACTIVE);

        if (hasActiveBookings) {
            log.error(ERROR_ACTIVE_BOOKINGS, id);
            throw new CarServiceException("Невозможно удалить машину с ID " + id + " - есть активные бронирования");
        }

        for (com.example.carsharing1.entity.Booking booking : car.getBookings()) {
            booking.setCar(null);
        }

        car.getBookings().clear();

        carRepository.delete(car);
        log.info(CAR_DELETED, id);

        invalidateCache();
    }

    public List<CarDto> findCarsByComplexCriteria(String email, String featureName) {
        log.info(SEARCH_JPQL, email, featureName);

        List<Car> cars = carRepository.findCarsByComplexCriteria(email, featureName);
        return cars.stream()
                .map(CarMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public List<CarDto> findCarsByComplexCriteriaNative(String email, String featureName) {
        log.info(SEARCH_NATIVE, email, featureName);

        List<Car> cars = carRepository.findCarsByComplexCriteriaNative(email, featureName);
        return cars.stream()
                .map(CarMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public Page<CarDto> findCarsByComplexCriteriaPaged(String email, String featureName,
                                                       int page, int size, String sortBy, String sortDirection) {
        log.info(SEARCH_PAGED, page, size, sortBy, sortDirection);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Car> carPage = carRepository.findCarsByComplexCriteriaPaged(email, featureName, pageable);
        return carPage.map(CarMapper::toDto);
    }

    public List<CarDto> findCarsWithCache(String email, String featureName,
                                          int page, int size, String sortBy, String sortDirection) {


        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        CarSearchKey cacheKey = CarSearchKey.fromParams(email, featureName, pageable);

        log.info(SEARCH_WITH_CACHE, cacheKey);

        if (searchCache.containsKey(cacheKey)) {
            log.info(CACHE_HIT, cacheKey);
            return searchCache.get(cacheKey);
        }


        log.info(CACHE_MISS, cacheKey);
        List<CarDto> results = findCarsByComplexCriteria(email, featureName);


        searchCache.put(cacheKey, results);

        return results;
    }


    private void invalidateCache() {
        log.info(CACHE_INVALIDATED);
        searchCache.clear();
    }

    private Car prepareCar(CarDto carDto, Long locationId) {
        Car car = CarMapper.toEntity(carDto);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> {
                    log.error(LOCATION_NOT_FOUND, locationId);
                    return new LocationNotFoundException("Локация с ID " + locationId + " не найдена");
                });

        car.setLocation(location);
        car.setActive(true);
        return car;
    }

    private Car saveCarWithLocation(CarDto carDto, Long locationId) {
        Car car = prepareCar(carDto, locationId);
        Car savedCar = carRepository.save(car);
        log.info(CAR_SAVED, savedCar.getId());
        return savedCar;
    }

    private void addFeaturesToCar(Car car, List<Long> featureIds, boolean transactional) {
        Set<Feature> featuresToAdd = collectFeatures(featureIds, transactional);
        car.getFeatures().addAll(featuresToAdd);
        carRepository.save(car);
        log.info(FEATURES_ADDED, featuresToAdd.size(), car.getId());
    }

    private Set<Feature> collectFeatures(List<Long> featureIds, boolean transactional) {
        Set<Feature> featuresToAdd = new HashSet<>();

        for (int i = 0; i < featureIds.size(); i++) {
            Long featureId = featureIds.get(i);
            Feature feature = featureRepository.findById(featureId)
                    .orElseThrow(() -> {
                        log.error(FEATURE_NOT_FOUND, featureId);
                        return new FeatureNotFoundException("Особенность с ID " + featureId + " не найдена");
                    });

            featuresToAdd.add(feature);
            log.info(FEATURE_PREPARED, feature.getName());

            if (i == 2) {
                String errorMsg = transactional ? ERROR_TX_ROLLBACK : ERROR_ON_THIRD_FEATURE;
                log.error(errorMsg);
                throw new CarServiceException(errorMsg);
            }
        }

        return featuresToAdd;
    }

    private void updateCarFields(Car car, CarDto dto) {
        if (dto.getBrand() != null) {
            car.setBrand(dto.getBrand());
        }
        if (dto.getModel() != null) {
            car.setModel(dto.getModel());
        }
        if (dto.getPricePerMinute() != null) {
            car.setPricePerMinute(dto.getPricePerMinute());
        }
        if (dto.getLicensePlate() != null) {
            car.setLicensePlate(dto.getLicensePlate());
        }
        if (dto.getYear() != null) {
            car.setYear(dto.getYear());
        }
        if (dto.getFuelLevel() != null) {
            car.setFuelLevel(dto.getFuelLevel());
        }
    }

    public List<CarDto> getAllCarsWithNPlusOneProblem() {
        log.info(N_PLUS_ONE_DEMO);
        List<Car> cars = carRepository.findAll();
        List<CarDto> result = new ArrayList<>();

        for (Car car : cars) {
            Location location = car.getLocation();
            Set<Feature> features = car.getFeatures();

            log.info(CAR_INFO_LOG,
                    car.getId(),
                    location != null ? location.getCity() : "не указана",
                    features.size(),
                    car.isAvailable());

            result.add(CarMapper.toDto(car));
        }
        return result;
    }

    public Optional<CarDto> getCarByIdWithEntityGraph(Long id) {
        log.info(ENTITY_GRAPH_SOLUTION);
        return carRepository.findByIdWithDetails(id)
                .map(CarMapper::toDto);
    }

    public List<CarDto> getAllActiveCarsWithFetchJoin() {
        log.info(FETCH_JOIN_SOLUTION);
        List<Car> cars = carRepository.findAllActiveWithDetails();
        return cars.stream()
                .map(CarMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public void saveCarWithFeaturesWithoutTransaction(CarDto carDto, List<Long> featureIds, Long locationId) {
        log.info(WITHOUT_TX_DEMO);

        Car savedCar = saveCarWithLocation(carDto, locationId);
        addFeaturesToCar(savedCar, featureIds, false);

        log.info(FEATURES_ADDED_SUCCESS);
    }

    @Transactional
    public void saveCarWithFeaturesWithTransaction(CarDto carDto, List<Long> featureIds, Long locationId) {
        log.info(WITH_TX_DEMO);

        Car savedCar = saveCarWithLocation(carDto, locationId);
        addFeaturesToCar(savedCar, featureIds, true);

        log.info(TX_COMMITTED);
    }
}