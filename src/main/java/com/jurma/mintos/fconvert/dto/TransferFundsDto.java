package com.jurma.mintos.fconvert.dto;

import com.jurma.mintos.fconvert.validator.CurrencyAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Currency;

public record TransferFundsDto(@NotNull Long sourceAccountId, @NotNull Long targetAccountId,
                               @CurrencyAllowed Currency targetCurrency, @NotNull @Positive BigDecimal amount) {
}