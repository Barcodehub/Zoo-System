package com.nelumbo.zoo_api.exception;

public class IllegalOperationException extends RuntimeException {
    private String field;

    public IllegalOperationException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}