package com.example.gym.exception;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String errorMessage) {
        super(errorMessage);
    }
}
