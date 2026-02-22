package com.lucastudios.EconomyPlus.model;

import java.math.BigDecimal;

public sealed interface TransactionResult permits TransactionResult.Success, TransactionResult.Failure {
    boolean isSuccess();

    record Success(BigDecimal amount, BigDecimal newBalance) implements TransactionResult {
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    record Failure(BigDecimal amount, BigDecimal balance, FailureReason reason, String message) implements TransactionResult {
        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    enum FailureReason {
        CURRENCY_NOT_FOUND,
        PLAYER_NOT_FOUND,
        INSUFFICIENT_FUNDS,
        INVALID_AMOUNT,
        CANNOT_PAY_SELF,
        FLAT_TAX_TOO_HIGH
    }
}
