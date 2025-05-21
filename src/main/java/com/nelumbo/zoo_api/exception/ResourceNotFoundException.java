package com.nelumbo.zoo_api.exception;

public class ResourceNotFoundException extends RuntimeException {
    private String field;

    public ResourceNotFoundException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}