package com.lucastudios.EconomyPlus.model;

import java.math.BigDecimal;

public record PayResult(
    boolean success,
    BigDecimal gross,
    BigDecimal tax,
    BigDecimal net,
    String mode,
    TransactionResult.FailureReason failureReason
) {
    public static PayResult success(BigDecimal gross, BigDecimal tax, BigDecimal net, String mode) {
        return new PayResult(true, gross, tax, net, mode, null);
    }

    public static PayResult failure(TransactionResult.FailureReason reason) {
        return new PayResult(false, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, reason);
    }
}
