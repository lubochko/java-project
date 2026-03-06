package com.example.carsharing1.enums;
import lombok.Getter;

@Getter
public enum CarStatus {
    AVAILABLE("Доступен"),
    BUSY("Занят"),
    MAINTENANCE("На обслуживании");

    private final String description;

    CarStatus(String description) {
        this.description = description;
    }

}