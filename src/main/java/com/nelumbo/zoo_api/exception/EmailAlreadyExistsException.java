package com.nelumbo.zoo_api.exception;


import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {
    private final String fieldName;

    public EmailAlreadyExistsException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

}