package com.nelumbo.zoo_api.validation.validators;


import com.nelumbo.zoo_api.dto.ZoneRequest;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.validation.annotations.UniqueSpeciesName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueZonesNameValidator implements ConstraintValidator<UniqueSpeciesName, ZoneRequest> {

    private final SpeciesRepository speciesRepository;

    @Override
    public boolean isValid(ZoneRequest request, ConstraintValidatorContext context) {
        if (request == null || request.name() == null) {
            return true;
        }
        return !speciesRepository.existsByName(request.name());
    }
}