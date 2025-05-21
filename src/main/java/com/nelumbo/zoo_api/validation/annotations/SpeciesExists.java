package com.nelumbo.zoo_api.validation.annotations;


import com.nelumbo.zoo_api.validation.validators.SpeciesExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = SpeciesExistsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpeciesExists {
    String message() default "Species does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}