package com.example.carsharing1.mapper;

import com.example.carsharing1.dto.CarDto;
import com.example.carsharing1.entity.Car;

public class CarMapper {

    private CarMapper() {
    }

    public static CarDto toDto(Car car) {

        if (car == null) {
            return null;
        }

        CarDto dto = new CarDto();
        dto.setId(car.getId());
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setPricePerMinute(car.getPricePerMinute());

        return dto;
    }
}