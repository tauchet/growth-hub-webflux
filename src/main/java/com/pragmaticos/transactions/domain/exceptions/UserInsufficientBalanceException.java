package com.pragmaticos.transactions.domain.exceptions;

import lombok.Getter;

@Getter
public class UserInsufficientBalanceException extends RuntimeException implements UserException {

    private final String userId;

    public UserInsufficientBalanceException(String userId, String message) {
        super(message);
        this.userId = userId;
    }

}
