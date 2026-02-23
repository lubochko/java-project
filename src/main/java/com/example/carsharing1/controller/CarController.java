package com.example.carsharing1.controller;

import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.service.CarService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/cars")
@RestController
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping("/{id}")
    public CarDto getCarById(@PathVariable int id) {
        return carService.getCarById(id);
    }

    @GetMapping
    public List<CarDto> getCarsByBrand(
            @RequestParam(required = false) String brand) {
        return carService.getCarsByBrand(brand);
    }
}

