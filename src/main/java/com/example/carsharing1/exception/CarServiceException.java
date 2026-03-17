package com.example.carsharing1.exception;

public class CarServiceException extends RuntimeException {
    public CarServiceException(String message) {
        super(message);
    }
}