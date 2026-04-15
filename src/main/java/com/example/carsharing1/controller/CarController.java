package com.example.carsharing1.controller;

import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Tag(name = "Автомобили", description = "Управление автомобилями в системе каршеринга")
public class CarController {

    private final CarService carService;

    private static final String LOG_GET_ALL = "GET /api/cars - запрос всех машин";
    private static final String LOG_GET_BY_ID = "GET /api/cars/{} - запрос машины по ID";
    private static final String LOG_CREATE = "POST /api/cars - создание новой машины: {}";
    private static final String LOG_UPDATE = "PUT /api/cars/{} - обновление машины";
    private static final String LOG_PATCH_STATUS = "PATCH /api/cars/{}/status - обновление статуса машины: active={}";
    private static final String LOG_DELETE = "DELETE /api/cars/{} - удаление машины";
    private static final String LOG_COMPLEX_SEARCH = "GET /api/cars/search - сложный поиск: email={}, feature={}";
    private static final String LOG_COMPLEX_SEARCH_NATIVE = "GET /api/cars/search/native " +
            "- native поиск: email={}, feature={}";
    private static final String LOG_PAGED_SEARCH = "GET /api/cars/search/paged - поиск с пагинацией: email={}, " +
            "feature={}, page={}, size={}, sortBy={}, sortDir={}";
    private static final String LOG_CACHED_SEARCH = "GET /api/cars/search/cached - поиск с кэшем: email={}, " +
            "feature={}, page={}, size={}, sortBy={}, sortDir={}";
    private static final String LOG_NPLUSONE_DEMO = "GET /api/cars/nplus1-demo - демонстрация проблемы N+1";
    private static final String LOG_ENTITY_GRAPH = "GET /api/cars/{}/details - решение N+1 через @EntityGraph";
    private static final String LOG_FETCH_JOIN = "GET /api/cars/active/details - решение N+1 через FETCH JOIN";
    private static final String LOG_WITHOUT_TX = "POST /api/cars/with-features - сохранение без @Transactional";
    private static final String LOG_WITH_TX = "POST /api/cars/with-features - сохранение с @Transactional";
    private static final String LOG_CAR_NOT_FOUND = "Машина с ID {} не найдена";
    private static final String LOG_CAR_DELETED = "Машина с ID {} успешно удалена";

    @Operation(summary = "Получить все машины", description = "Возвращает список всех автомобилей")
    @GetMapping
    public ResponseEntity<List<CarDto>> getAllCars() {
        log.info(LOG_GET_ALL);
        return ResponseEntity.ok(carService.getAllCars());
    }

    @Operation(summary = "Получить активные машины", description = "Возвращает список только активных автомобилей")
    @GetMapping("/active")
    public ResponseEntity<List<CarDto>> getAllActiveCars() {
        log.info("GET /api/cars/active - запрос всех активных машин");
        return ResponseEntity.ok(carService.getAllActiveCars());
    }

    @Operation(summary = "Получить доступные машины", description = "Возвращает список машин без активных бронирований")
    @GetMapping("/available")
    public ResponseEntity<List<CarDto>> getAvailableCars() {
        log.info("GET /api/cars/available - запрос доступных машин");
        return ResponseEntity.ok(carService.getAvailableCars());
    }

    @Operation(summary = "Получить машину по ID", description = "Возвращает автомобиль по указанному ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Автомобиль " +
            "найден"), @ApiResponse(responseCode = "404", description = "Автомобиль не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long id) {
        log.info(LOG_GET_BY_ID, id);
        return carService.getCarById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn(LOG_CAR_NOT_FOUND, id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Создать машину", description = "Создает новый автомобиль")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Автомобиль " +
            "создан"), @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarDto carDto) {
        log.info(LOG_CREATE, carDto.getBrand() + " " + carDto.getModel());
        CarDto createdCar = carService.createCar(carDto);
        return new ResponseEntity<>(createdCar, HttpStatus.CREATED);
    }

    @Operation(summary = "Создать машину с локацией", description = "Создает новый автомобиль с указанием локации")
    @PostMapping("/with-location")
    public ResponseEntity<CarDto> createCarWithLocation(
            @Valid @RequestBody CarDto carDto,
            @RequestParam Long locationId) {
        log.info("POST /api/cars/with-location - создание машины с локацией ID: {}", locationId);
        CarDto createdCar = carService.createCarWithLocation(carDto, locationId);
        return new ResponseEntity<>(createdCar, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить машину", description = "Обновляет данные существующего автомобиля")
    @PutMapping("/{id}")
    public ResponseEntity<CarDto> updateCar(@PathVariable Long id, @Valid @RequestBody CarDto carDto) {
        log.info(LOG_UPDATE, id);
        return carService.updateCar(id, carDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn(LOG_CAR_NOT_FOUND, id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Обновить статус машины", description = "Обновляет статус активности автомобиля")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CarDto> updateCarStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        log.info(LOG_PATCH_STATUS, id, active);
        return carService.updateCarStatus(id, active)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn(LOG_CAR_NOT_FOUND, id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Удалить машину", description = "Удаляет автомобиль по ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Автомобиль " +
            "удален"), @ApiResponse(responseCode = "404", description = "Автомобиль не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        log.info(LOG_DELETE, id);

        if (carService.getCarById(id).isEmpty()) {
            log.warn(LOG_CAR_NOT_FOUND, id);
            return ResponseEntity.notFound().build();
        }

        carService.deleteCar(id);
        log.info(LOG_CAR_DELETED, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Демонстрация проблемы N+1", description = "Показывает проблему множественных запросов к БД")
    @GetMapping("/nplus1-demo")
    public ResponseEntity<List<CarDto>> demonstrateNPlusOne() {
        log.info(LOG_NPLUSONE_DEMO);
        return ResponseEntity.ok(carService.getAllCarsWithNPlusOneProblem());
    }

    @Operation(summary = "Решение N+1 через EntityGraph", description = "Загружает машину с деталями одним запросом")
    @GetMapping("/{id}/details")
    public ResponseEntity<CarDto> getCarWithDetails(@PathVariable Long id) {
        log.info(LOG_ENTITY_GRAPH, id);
        return carService.getCarByIdWithEntityGraph(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn(LOG_CAR_NOT_FOUND, id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Решение N+1 через FETCH JOIN", description = "Загружает активные машины с деталями")
    @GetMapping("/active/details")
    public ResponseEntity<List<CarDto>> getActiveCarsWithDetails() {
        log.info(LOG_FETCH_JOIN);
        return ResponseEntity.ok(carService.getAllActiveCarsWithFetchJoin());
    }

    @Operation(summary = "Сохранение с особенностями (демонстрация транзакций)")
    @PostMapping("/with-features")
    public ResponseEntity<String> saveCarWithFeatures(
            @Valid @RequestBody CarDto carDto,
            @RequestParam List<Long> featureIds,
            @RequestParam Long locationId,
            @RequestParam boolean useTransaction) {

        try {
            if (useTransaction) {
                log.info(LOG_WITH_TX);
                carService.saveCarWithFeaturesWithTransaction(carDto, featureIds, locationId);
                return ResponseEntity.ok("Машина успешно сохранена с транзакцией (данные будут откатаны при ошибке)");
            } else {
                log.info(LOG_WITHOUT_TX);
                carService.saveCarWithFeaturesWithoutTransaction(carDto, featureIds, locationId);
                return ResponseEntity.ok("Машина успешно сохранена без транзакции (частичное сохранение при ошибке)");
            }
        } catch (Exception e) {
            log.error("Ошибка при сохранении машины с особенностями: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage());
        }
    }

    @Operation(summary = "Сложный JPQL запрос", description = "Поиск машин по email пользователя и/или особенности")
    @GetMapping("/search")
    public ResponseEntity<List<CarDto>> searchCars(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String feature) {

        log.info(LOG_COMPLEX_SEARCH, email, feature);
        List<CarDto> cars = carService.findCarsByComplexCriteria(email, feature);
        return ResponseEntity.ok(cars);
    }

    @Operation(summary = "Native SQL запрос", description = "Поиск машин через нативный SQL запрос")
    @GetMapping("/search/native")
    public ResponseEntity<List<CarDto>> searchCarsNative(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String feature) {

        log.info(LOG_COMPLEX_SEARCH_NATIVE, email, feature);
        List<CarDto> cars = carService.findCarsByComplexCriteriaNative(email, feature);
        return ResponseEntity.ok(cars);
    }

    @Operation(summary = "Поиск с пагинацией", description = "Поиск машин с поддержкой постраничного вывода")
    @GetMapping("/search/paged")
    public ResponseEntity<Page<CarDto>> searchCarsPaged(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String feature,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.info(LOG_PAGED_SEARCH, email, feature, page, size, sortBy, sortDirection);

        Page<CarDto> carPage = carService.findCarsByComplexCriteriaPaged(
                email, feature, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(carPage);
    }

    @Operation(summary = "Поиск с кэшированием", description = "Поиск машин с использованием in-memory кэша")
    @GetMapping("/search/cached")
    public ResponseEntity<List<CarDto>> searchCarsCached(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String feature,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.info(LOG_CACHED_SEARCH, email, feature, page, size, sortBy, sortDirection);

        List<CarDto> cars = carService.findCarsWithCache(
                email, feature, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(cars);
    }
}