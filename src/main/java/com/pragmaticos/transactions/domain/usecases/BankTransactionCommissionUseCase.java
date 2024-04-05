package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.model.Transaction;
import reactor.core.publisher.Mono;

public interface BankTransactionCommissionUseCase {

    Mono<Transaction> createBankTransaction(String userId, String description, double commission);

}
