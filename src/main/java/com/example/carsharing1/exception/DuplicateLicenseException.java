package com.example.carsharing1.exception;

public class DuplicateLicenseException extends RuntimeException {
    public DuplicateLicenseException(String message) {
        super(message);
    }
}
