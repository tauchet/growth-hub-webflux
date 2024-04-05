package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;

@RequiredArgsConstructor
public class BankTransactionCommissionUseCaseImpl implements BankTransactionCommissionUseCase {

    private final UserPort userPort;
    private final TransactionPort transactionPort;

    @Override
    public Mono<Transaction> createBankTransaction(String userId, String description, double commission, boolean removeFromUser) {

        return this.transactionPort
                .save(Transaction.builder()
                        .date(LocalDateTime.now())
                        .value(commission)
                        .userId("1")
                        .fromUserId(userId)
                        .description(description)
                        .origin(TransactionOrigin.BANK_TRANSFER)
                        .state(TransactionState.PENDING)
                        .build())
                .flatMap(ts -> {
                    if (removeFromUser) {
                        return this.userPort.sumBalanceById(ts.getFromUserId(), -1 * ts.getValue()).thenReturn(ts);
                    }
                    return Mono.just(ts);
                })
                .flatMap(ts -> this.userPort.sumBalanceById(ts.getUserId(), ts.getValue()).thenReturn(ts))
                .flatMap(ts -> {

                    System.out.println("Complete?!");

                    ts.setState(TransactionState.COMPLETE);
                    return this.transactionPort.save(ts);
                });
    }

}
