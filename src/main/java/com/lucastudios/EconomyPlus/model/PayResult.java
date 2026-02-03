package com.lucastudios.EconomyPlus.model;

public record PayResult(
    boolean success,
    long gross,
    long tax,
    long net,
    String mode,
    TransactionResult.FailureReason failureReason
) {
    public static PayResult success(long gross, long tax, long net, String mode) {
        return new PayResult(true, gross, tax, net, mode, null);
    }

    public static PayResult failure(TransactionResult.FailureReason reason) {
        return new PayResult(false, 0, 0, 0, null, reason);
    }
}
