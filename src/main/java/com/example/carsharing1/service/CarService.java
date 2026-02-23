package com.example.carsharing1.service;

import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.mapper.CarMapper;
import com.example.carsharing1.repository.CarRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CarService {

    private final CarRepository carRepository;

    public List<CarDto> getAllCars() {
        return carRepository.findAll()
                .stream()
                .map(CarMapper::toDto)
                .toList();
    }

    public CarDto getCarById(int id) {

        Car car = carRepository.findAll()
                .stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);

        return CarMapper.toDto(car);
    }

    public List<CarDto> getCarsByBrand(String brand) {

        if (brand == null || brand.isEmpty()) {
            return getAllCars();
        }

        return carRepository.findAll()
                .stream()
                .filter(car -> car.getBrand().equalsIgnoreCase(brand))
                .map(CarMapper::toDto)
                .toList();
    }
}

