package com.example.carsharing1.enums;
import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Ожидает оплаты"),
    PAID("Оплачено"),
    REFUNDED("Возврат");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

}