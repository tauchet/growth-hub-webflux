package com.pragmaticos.transactions.domain.ports;

import com.pragmaticos.transactions.domain.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionPort {

    Flux<Transaction> findAllByUserId(String userId);

    Mono<Double> dailySumOfTransactions(String userId);

    Mono<Transaction> save(Transaction transaction);

    Mono<Transaction> findById(String id);
}
