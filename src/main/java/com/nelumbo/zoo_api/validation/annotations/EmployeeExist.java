package com.nelumbo.zoo_api.validation.annotations;

import com.nelumbo.zoo_api.validation.validators.EmployeeExistValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmployeeExistValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmployeeExist {
    String message() default "El socio no existe";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}