package com.pragmaticos.transactions.domain.model;

public enum TransactionState {

    PENDING,
    COMPLETE,

    PROCESSING_CANCELLATION,
    CANCELLED

}
