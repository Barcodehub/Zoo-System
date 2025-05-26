package com.nelumbo.zoo_api.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException{
    private final String field;

    public BadRequestException(String field, String message) {
        super(message);
        this.field = field;
    }
}
