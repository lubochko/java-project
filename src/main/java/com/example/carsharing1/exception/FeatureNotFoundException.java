package com.example.carsharing1.exception;

public class FeatureNotFoundException extends RuntimeException {
    public FeatureNotFoundException(String message) {
        super(message);
    }
}
