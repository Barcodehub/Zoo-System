package com.nelumbo.zoo_api.validation.validators;

import com.nelumbo.zoo_api.repository.ZoneRepository;
import com.nelumbo.zoo_api.validation.annotations.ZonaExist;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ZoneExistsValidator implements ConstraintValidator<ZonaExist, Long> {

    private final ZoneRepository zoneRepository;

    @Override
    public boolean isValid(Long zoneId, ConstraintValidatorContext context) {
        if (zoneId == null) {
            return true; // null values are considered valid (use @NotNull if needed)
        }
        return zoneRepository.existsById(zoneId);
    }
}
