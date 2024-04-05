package com.pragmaticos.transactions.domain.exceptions;

import lombok.Getter;

@Getter
public class UserNotExistsException extends RuntimeException implements UserException {

    private final String userId;

    public UserNotExistsException(String userId, String message) {
        super(message);
        this.userId = userId;
    }

}
