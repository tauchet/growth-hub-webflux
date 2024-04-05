package com.pragmaticos.transactions.domain.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class UserNotExistsException extends RuntimeException {

    private final String id;

    public UserNotExistsException(String id, String message) {
        super(message);
        this.id = id;
    }

}
