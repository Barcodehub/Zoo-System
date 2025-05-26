package com.nelumbo.zoo_api.exception;

import lombok.Getter;

@Getter
public class IllegalOperationException extends RuntimeException {
    private final String field;

    public IllegalOperationException(String message, String field) {
        super(message);
        this.field = field;
    }

}