package com.example.carsharing1.enums;
import lombok.Getter;

@Getter
public enum BookingStatus {
    ACTIVE("Активно"),
    COMPLETED("Завершено"),
    CANCELLED("Отменено");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

}