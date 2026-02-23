package com.example.carsharing1.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Car {

    private int id;
    private String brand;
    private String model;
    private double pricePerMinute;
    private String status;
}
