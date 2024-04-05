package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.model.Transaction;
import reactor.core.publisher.Flux;

public interface TransactionsByUserUseCase {

    Flux<Transaction> findAllByUserId(String userId);

}
