package com.example.carsharing1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureDto {
    private Long id;
    private String name;
    private String description;
    private String icon;
}