package com.nelumbo.zoo_api.validation.validators;


import com.nelumbo.zoo_api.repository.UserRepository;
import com.nelumbo.zoo_api.validation.annotations.EmployeeExist;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeExistValidator implements ConstraintValidator<EmployeeExist, Integer> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(Integer socioId, ConstraintValidatorContext context) {
        if (socioId == null) {
            return true; // Permitir null, usar @NotNull para hacerlo obligatorio
        }
        return userRepository.existsById(socioId);
    }
}