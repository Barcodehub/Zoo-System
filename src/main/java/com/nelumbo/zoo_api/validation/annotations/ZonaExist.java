package com.nelumbo.zoo_api.validation.annotations;

import com.nelumbo.zoo_api.validation.validators.ZoneExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ZoneExistsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZonaExist {
    String message() default "No existe la Zona";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}