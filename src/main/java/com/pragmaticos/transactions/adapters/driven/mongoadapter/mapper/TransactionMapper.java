package com.pragmaticos.transactions.adapters.driven.mongoadapter.mapper;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.TransactionEntity;
import com.pragmaticos.transactions.domain.model.Transaction;

public class TransactionMapper {

    public static Transaction mapToTransaction(TransactionEntity transactionEntity) {
        return Transaction.builder()
                .id(transactionEntity.getId())
                .userId(transactionEntity.getUserId())
                .fromUserId(transactionEntity.getFromUserId())
                .value(transactionEntity.getValue())
                .date(transactionEntity.getDate())
                .state(transactionEntity.getState())
                .origin(transactionEntity.getOrigin())
                .commission(transactionEntity.getCommission())
                .description(transactionEntity.getDescription())
                .build();
    }

    public static TransactionEntity mapToTransactionEntity(Transaction transaction) {
        return TransactionEntity.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .fromUserId(transaction.getFromUserId())
                .value(transaction.getValue())
                .origin(transaction.getOrigin())
                .date(transaction.getDate())
                .state(transaction.getState())
                .commission(transaction.getCommission())
                .description(transaction.getDescription())
                .build();
    }

}
