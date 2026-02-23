package com.example.carsharing1.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Location {

    private int id;
    private String city;
    private String address;
}

