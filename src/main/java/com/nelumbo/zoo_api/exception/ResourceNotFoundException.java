package com.nelumbo.zoo_api.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String field;

    public ResourceNotFoundException(String message, String field) {
        super(message);
        this.field = field;
    }

}