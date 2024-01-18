package com.jurma.mintos.fconvert.validator;

import com.jurma.mintos.fconvert.configuration.AllowedCurrencyCodes;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CurrencyValidatorTest {

    @Test
    public void testIsValidForAllowed() {
        var validator = new CurrencyValidator();
        assertFalse(validator.isValid( Currency.getInstance("AUD"),  mock(ConstraintValidatorContext.class)));
        assertTrue(validator.isValid( Currency.getInstance(AllowedCurrencyCodes.EUR.name()),  mock(ConstraintValidatorContext.class)));
        assertTrue(validator.isValid( Currency.getInstance(AllowedCurrencyCodes.USD.name()),  mock(ConstraintValidatorContext.class)));
        assertTrue(validator.isValid( Currency.getInstance(AllowedCurrencyCodes.GBP.name()),  mock(ConstraintValidatorContext.class)));
    }

}