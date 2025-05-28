package com.nelumbo.zoo_api.exception;


import com.nelumbo.zoo_api.dto.errors.ErrorDetailDTO;
import com.nelumbo.zoo_api.dto.errors.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseError(HttpMessageNotReadableException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Invalid JSON format: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorDetailDTO errorDetail = new ErrorDetailDTO(
                "400",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(null, List.of(errorDetail)));
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorDetailDTO> errors = ex.getFieldErrors().stream()
                .map(fieldError -> new ErrorDetailDTO(
                        "400",
                        fieldError.getDefaultMessage(),
                        fieldError.getField()
                ))
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(null, errors));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorDetailDTO> errors = ex.getConstraintViolations().stream()
                .map(v -> new ErrorDetailDTO(
                        "400",  // Bad Request
                        v.getMessage(),
                        v.getPropertyPath().toString().split("\\.")[1]))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(null, errors));
    }


    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationFailed(AuthenticationFailedException ex) {
        ErrorDetailDTO errorDetail = new ErrorDetailDTO(
                "401",
                ex.getMessage(),
                ex.getField()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(null, List.of(errorDetail)));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailExists(EmailAlreadyExistsException ex) {
        ErrorDetailDTO error = new ErrorDetailDTO(
                "400", // "USER_001"
                ex.getMessage(),
                ex.getFieldName() // "email"
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(null, List.of(error)));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleInternalError(Exception ex) {
        ErrorDetailDTO errorDetail = new ErrorDetailDTO(
                "500",
                "Internal server error",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO(null, List.of(errorDetail)));
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        ErrorDetailDTO errorDetail = new ErrorDetailDTO(
                "403",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO(null, List.of(errorDetail)));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<ErrorDetailDTO> errors = new ArrayList<>();

        // Extraemos los argumentos del mensaje de error
        Object[] detailArgs = ex.getDetailMessageArguments();
        if (detailArgs != null) {
            for (Object arg : detailArgs) {
                if (arg instanceof String str) {
                    errors.add(new ErrorDetailDTO(
                            "400",
                            str,
                            extractParameterName(ex)
                    ));
                }
            }
        }

        // Si no encontramos detalles, usamos el mensaje general
        if (errors.isEmpty()) {
            ex.getMessage();
            errors.add(new ErrorDetailDTO(
                    "400",
                    ex.getMessage(),
                    null
            ));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(null, errors));
    }

    private String extractParameterName(HandlerMethodValidationException ex) {
        try {
            // Intento reflexivo para obtener el nombre del parámetro en versiones antiguas
            MethodParameter[] parameters = (MethodParameter[]) ex.getClass()
                    .getMethod("getMethodParameters").invoke(ex);
            if (parameters != null && parameters.length > 0) {
                return parameters[0].getParameterName();
            }
        } catch (Exception e) {
            // Fallback si falla la reflexión
            return null;
        }
        return null;
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorDetailDTO response = new ErrorDetailDTO(
                "404",
                ex.getMessage(),
                ex.getField()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(null, List.of(response)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorDetailDTO errorDetail = new ErrorDetailDTO(
                "400",
                "El ID debe ser un número válido",
                "id"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(null, List.of(errorDetail)));
    }


    @ExceptionHandler(IllegalOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalOperation(IllegalOperationException ex) {
        ErrorDetailDTO response = new ErrorDetailDTO(
                "400",
                ex.getMessage(),
                ex.getField()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(null, List.of(response)));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(IllegalStateException ex) {
        ErrorDetailDTO response = new ErrorDetailDTO(
                "409",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(null, List.of(response)));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingHeaders(MissingRequestHeaderException ex) {
        ErrorDetailDTO error = new ErrorDetailDTO(
                "400",
                "El header " + ex.getHeaderName() + " es requerido",
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(null, List.of(error)));
    }
}