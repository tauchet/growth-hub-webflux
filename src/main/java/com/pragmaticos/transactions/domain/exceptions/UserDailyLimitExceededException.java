package com.pragmaticos.transactions.domain.exceptions;

import lombok.Getter;

@Getter
public class UserDailyLimitExceededException extends RuntimeException implements UserException {

    private final String userId;

    public UserDailyLimitExceededException(String userId, String message) {
        super(message);
        this.userId = userId;
    }

}
