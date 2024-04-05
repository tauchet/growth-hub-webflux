package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.requests.UserCreateTransactionRequest;
import reactor.core.publisher.Mono;

public interface UserCreateTransactionUseCase {

    Mono<Transaction> createTransaction(UserCreateTransactionRequest data);

}
