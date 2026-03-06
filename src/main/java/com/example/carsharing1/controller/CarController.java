package com.example.carsharing1.controller;

import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public List<CarDto> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long id) {
        CarDto car = carService.getCarById(id);
        return car != null ? ResponseEntity.ok(car) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CarDto> createCar(@RequestBody CarDto carDto) {
        return new ResponseEntity<>(carService.createCar(carDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarDto> updateCar(@PathVariable Long id, @RequestBody CarDto carDto) {
        CarDto updatedCar = carService.updateCar(id, carDto);
        return updatedCar != null ? ResponseEntity.ok(updatedCar) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/nplus1-demo")
    public List<CarDto> demonstrateNPlusOne() {
        return carService.getAllCarsWithNPlusOneProblem();
    }


    @GetMapping("/{id}/details")
    public ResponseEntity<CarDto> getCarWithDetails(@PathVariable Long id) {
        CarDto car = carService.getCarByIdWithEntityGraph(id);
        return car != null ? ResponseEntity.ok(car) : ResponseEntity.notFound().build();
    }


    @GetMapping("/available/details")
    public List<CarDto> getAvailableCarsWithDetails() {
        return carService.getAvailableCarsWithFetchJoin();
    }


    @PostMapping("/with-features")
    public ResponseEntity<String> saveCarWithFeatures(
            @RequestBody CarDto carDto,
            @RequestParam List<Long> featureIds,
            @RequestParam Long locationId,
            @RequestParam boolean useTransaction) {

        try {
            if (useTransaction) {
                carService.saveCarWithFeaturesWithTransaction(carDto, featureIds, locationId);
                return ResponseEntity.ok("Машина успешно сохранена с транзакцией");
            } else {
                carService.saveCarWithFeaturesWithoutTransaction(carDto, featureIds, locationId);
                return ResponseEntity.ok("Машина успешно сохранена без транзакции");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage());
        }
    }
}