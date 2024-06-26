package com.jurma.codesamples.fconvert.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = CurrencyValidator.class)
@Documented
public @interface CurrencyAllowed {
    String message() default "Supplied currency code not allowed or empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}