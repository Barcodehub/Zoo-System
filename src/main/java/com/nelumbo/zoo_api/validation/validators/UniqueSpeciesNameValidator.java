package com.nelumbo.zoo_api.validation.validators;

import com.nelumbo.zoo_api.dto.SpeciesRequest;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.validation.annotations.UniqueSpeciesName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueSpeciesNameValidator implements ConstraintValidator<UniqueSpeciesName, String> {

    private final SpeciesRepository speciesRepository;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null) return true;

        return !speciesRepository.existsByName(name);
    }
}

