package com.example.carsharing1.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Booking {

    private int id;
    private int userId;
    private int carId;
    private String startTime;
    private String endTime;
}
