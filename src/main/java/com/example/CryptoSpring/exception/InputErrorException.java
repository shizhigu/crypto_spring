package com.example.CryptoSpring.exception;

public class InputErrorException extends RuntimeException {
    public InputErrorException() {
        super();
    }

    public InputErrorException(String message) {
        super(message);
    }
}
