package com.nelumbo.zoo_api.validation.validators;


import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.validation.annotations.SpeciesExists;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpeciesExistsValidator implements ConstraintValidator<SpeciesExists, Long> {

    private final SpeciesRepository speciesRepository;

    @Override
    public boolean isValid(Long speciesId, ConstraintValidatorContext context) {
        if (speciesId == null) {
            return true; // null values are considered valid (use @NotNull if needed)
        }
        return speciesRepository.existsById(speciesId);
    }
}