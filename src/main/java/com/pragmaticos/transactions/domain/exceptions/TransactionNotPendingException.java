package com.pragmaticos.transactions.domain.exceptions;

import lombok.Getter;

@Getter
public class TransactionNotPendingException extends RuntimeException {

    private final String id;

    public TransactionNotPendingException(String id, String message) {
        super(message);
        this.id = id;
    }

}

