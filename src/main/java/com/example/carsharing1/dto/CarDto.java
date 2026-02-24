package com.example.carsharing1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*Переделать потому что выдаются ошибки*/
@AllArgsConstructor
@Data
@NoArgsConstructor
public class CarDto {

    private int id;
    private String brand;
    private String model;
    private double pricePerMinute;
}

