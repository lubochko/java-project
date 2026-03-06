package com.example.carsharing1.dto;

import com.example.carsharing1.enums.CarStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDto {
    private Long id;
    private String brand;
    private String model;
    private Double pricePerMinute;
    private CarStatus status;
    private String licensePlate;
    private Integer year;
    private Double fuelLevel;
    private String locationCity;
    private String locationAddress;
    private Set<String> features;
}