package com.unihub.shared.validation;

import com.unihub.shared.repository.CountryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountryIdValidator implements ConstraintValidator<ValidCountryId, Integer> {

    private final CountryRepository countryRepository;

    @Override
    public boolean isValid(Integer countryId, ConstraintValidatorContext context) {
        if (countryId == null) return true;
        return countryRepository.existsById(countryId);
    }
}