package com.nelumbo.zoo_api.validation.annotations;


import com.nelumbo.zoo_api.validation.validators.AnimalExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = AnimalExistsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnimalExists {
    String message() default "Animal does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}