package com.jurma.codesamples.fconvert.validator;

import com.jurma.codesamples.fconvert.configuration.AllowedCurrencyCodes;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Currency;

public class CurrencyValidator implements ConstraintValidator<CurrencyAllowed, Currency> {
    @Override
    public boolean isValid(Currency value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && Arrays.stream(AllowedCurrencyCodes.values())
                .map(Enum::name)
                .anyMatch(currencyName -> currencyName.equals(value.getCurrencyCode()));
    }
}
