package com.nelumbo.zoo_api.validation.validators;


import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.validation.annotations.AnimalExists;
import com.nelumbo.zoo_api.validation.annotations.SpeciesExists;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnimalExistsValidator implements ConstraintValidator<AnimalExists, Long> {

    private final AnimalRepository animalRepository;

    @Override
    public boolean isValid(Long animalId, ConstraintValidatorContext context) {
        if (animalId == null) {
            return true; // null values are considered valid (use @NotNull if needed)
        }
        return animalRepository.existsById(animalId);
    }
}