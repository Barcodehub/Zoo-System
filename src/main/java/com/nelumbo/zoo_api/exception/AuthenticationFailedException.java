package com.nelumbo.zoo_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationFailedException extends RuntimeException {
    private final String field;

    public AuthenticationFailedException(String message, String field) {
        super(message);
        this.field = field;
    }

}