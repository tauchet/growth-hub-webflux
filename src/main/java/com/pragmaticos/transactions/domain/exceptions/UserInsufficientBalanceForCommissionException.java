package com.pragmaticos.transactions.domain.exceptions;

import com.pragmaticos.transactions.domain.model.Transaction;
import lombok.Getter;

@Getter
public class UserInsufficientBalanceForCommissionException extends RuntimeException {

    private final String userId;
    private final Transaction transaction;

    public UserInsufficientBalanceForCommissionException(String userId, Transaction transaction, String message) {
        super(message);
        this.userId = userId;
        this.transaction = transaction;
    }

}
