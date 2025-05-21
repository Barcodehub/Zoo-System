package com.nelumbo.zoo_api.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
    private String field;

    public ResourceAlreadyExistsException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}