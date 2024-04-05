package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.requests.UserCancelTransactionRequest;
import reactor.core.publisher.Mono;

public interface UserCancelTransactionUserCase {

    Mono<Transaction> cancelTransaction(UserCancelTransactionRequest data);

}
