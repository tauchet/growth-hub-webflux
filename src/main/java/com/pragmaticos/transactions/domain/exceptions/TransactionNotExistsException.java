package com.pragmaticos.transactions.domain.exceptions;

import lombok.Getter;

@Getter
public class TransactionNotExistsException extends RuntimeException {

    private final String id;

    public TransactionNotExistsException(String id, String message) {
        super(message);
        this.id = id;
    }

}
