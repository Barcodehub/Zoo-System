package com.nelumbo.zoo_api.validation.annotations;

import com.nelumbo.zoo_api.validation.validators.UniqueSpeciesNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueSpeciesNameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueSpeciesName {
    String message() default "Species with this name already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}