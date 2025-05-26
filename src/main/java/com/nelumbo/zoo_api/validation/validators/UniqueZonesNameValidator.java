package com.nelumbo.zoo_api.validation.validators;


import com.nelumbo.zoo_api.repository.ZoneRepository;
import com.nelumbo.zoo_api.validation.annotations.UniqueZoneName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueZonesNameValidator implements ConstraintValidator<UniqueZoneName, String> {

    private final ZoneRepository zonesRepository;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null) return true;

        return !zonesRepository.existsByName(name);
    }
}