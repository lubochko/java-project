package com.example.carsharing1.mapper;

import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Feature;
import java.util.stream.Collectors;

public class CarMapper {

    private CarMapper() { }

    public static CarDto toDto(Car car) {
        if (car == null) {
            return null;
        }

        CarDto dto = new CarDto();
        dto.setId(car.getId());
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setPricePerMinute(car.getPricePerMinute());
        dto.setStatus(car.getStatus());
        dto.setLicensePlate(car.getLicensePlate());
        dto.setYear(car.getYear());
        dto.setFuelLevel(car.getFuelLevel());

        if (car.getLocation() != null) {
            dto.setLocationCity(car.getLocation().getCity());
            dto.setLocationAddress(car.getLocation().getAddress());
        }

        if (car.getFeatures() != null) {
            dto.setFeatures(car.getFeatures().stream()
                    .map(Feature::getName)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    public static Car toEntity(CarDto dto) {
        if (dto == null) {
            return null;
        }

        Car car = new Car();
        car.setId(dto.getId());
        car.setBrand(dto.getBrand());
        car.setModel(dto.getModel());
        car.setPricePerMinute(dto.getPricePerMinute());
        car.setStatus(dto.getStatus());
        car.setLicensePlate(dto.getLicensePlate());
        car.setYear(dto.getYear());
        car.setFuelLevel(dto.getFuelLevel());

        return car;
    }
}