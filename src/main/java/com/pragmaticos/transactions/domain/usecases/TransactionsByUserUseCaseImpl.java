package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class TransactionsByUserUseCaseImpl implements TransactionsByUserUseCase {

    private final TransactionPort transactionPort;

    @Override
    public Flux<Transaction> findAllByUserId(String userId) {
        return null;
    }

}
