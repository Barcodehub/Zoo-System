package com.nelumbo.zoo_api.validation.annotations;

import com.nelumbo.zoo_api.validation.validators.UniqueZonesNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueZonesNameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueZoneName {
        String message() default "Zones with this name already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}