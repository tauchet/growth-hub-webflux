package com.pragmaticos.transactions.domain.exceptions;

import lombok.Getter;

@Getter
public class TransactionNotExistsException extends RuntimeException implements TransactionException {

    private final String id;

    public TransactionNotExistsException(String id, String message) {
        super(message);
        this.id = id;
    }

    @Override
    public String getTransactionId() {
        return this.id;
    }

}
