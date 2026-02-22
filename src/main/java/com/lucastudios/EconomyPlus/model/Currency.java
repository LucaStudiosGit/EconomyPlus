package com.lucastudios.EconomyPlus.model;

import java.math.BigDecimal;

public record Currency(
    String currencyId,
    String name,
    String symbol,
    int decimals,
    BigDecimal startingBalance
) {
    public Currency {
        if (currencyId == null || currencyId.isBlank())
            throw new IllegalArgumentException("Currency ID cannot be null or empty");
        if (decimals < 0)
            throw new IllegalArgumentException("Decimals cannot be negative");
        if (startingBalance == null)
            startingBalance = BigDecimal.ZERO;
    }

    public String format(long minorUnits) {
        if (decimals == 0)
            return String.valueOf(minorUnits);

        BigDecimal divisor = BigDecimal.TEN.pow(decimals);
        BigDecimal amount = new BigDecimal(minorUnits).divide(divisor);
        return amount.toPlainString();
    }

    public String format(BigDecimal amount) {
        if (decimals == 0)
            return amount.toPlainString();

        BigDecimal divisor = BigDecimal.TEN.pow(decimals);
        BigDecimal minorUnits = amount.multiply(divisor);
        return minorUnits.toPlainString();
    }

    public BigDecimal toMinorUnits(BigDecimal amount) {
        if (decimals == 0)
            return amount;

        BigDecimal multiplier = BigDecimal.TEN.pow(decimals);
        return amount.multiply(multiplier);
    }
}
