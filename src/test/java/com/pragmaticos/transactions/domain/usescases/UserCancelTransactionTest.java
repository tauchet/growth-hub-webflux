package com.pragmaticos.transactions.domain.usescases;

import com.pragmaticos.transactions.domain.exceptions.*;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.model.requests.UserCancelTransactionRequest;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import com.pragmaticos.transactions.domain.usecases.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DisplayName("Cancelar una transacción.")
@ExtendWith(MockitoExtension.class)
public class UserCancelTransactionTest {

    @Mock
    TransactionPort transactionPort;

    @Mock
    UserPort userPort;

    @Mock
    BankTransactionCommissionUseCase bankTransactionCommissionUseCase;

    @InjectMocks
    UserCancelTransactionUserCaseImpl userCancelTransactionUserCase;

    String TRANSACTION_ID = "test";

    String SOURCE_USER_ID = "123";
    String DESTINATION_USER_ID = "345";

    @Test
    @DisplayName("La transacción no existe.")
    public void onNotFoundById() {

        Mockito.when(this.transactionPort.findById(TRANSACTION_ID)).thenReturn(Mono.empty());
        Mono<Transaction> reply = this.userCancelTransactionUserCase.cancelTransaction(new UserCancelTransactionRequest(
                TRANSACTION_ID
        ));

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof TransactionNotExistsException && ((TransactionNotExistsException) e).getId().equals(TRANSACTION_ID))
                .verify();

    }

    @Test
    @DisplayName("La transacción se encuentra pendiente.")
    public void onTransactionNotPending() {

        Mockito.when(this.transactionPort.findById(TRANSACTION_ID)).thenReturn(Mono.just(
                Transaction.builder()
                        .id(TRANSACTION_ID)
                        .description("Transacción de ejemplo")
                        .userId(DESTINATION_USER_ID)
                        .fromUserId(SOURCE_USER_ID)
                        .state(TransactionState.COMPLETE)
                        .date(LocalDateTime.now())
                        .value(100_000)
                        .build()
        ));

        Mono<Transaction> reply = this.userCancelTransactionUserCase.cancelTransaction(new UserCancelTransactionRequest(
                TRANSACTION_ID
        ));

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof TransactionNotPendingException && ((TransactionNotPendingException) e).getId().equals(TRANSACTION_ID))
                .verify();


    }

    @Test
    @DisplayName("La transacción se ha completado.")
    public void onTransactionSuccess() {

        Transaction transaction = Transaction.builder()
                .id(TRANSACTION_ID)
                .description("Transacción de ejemplo")
                .userId(DESTINATION_USER_ID)
                .fromUserId(SOURCE_USER_ID)
                .state(TransactionState.PENDING)
                .date(LocalDateTime.now())
                .value(100_000)
                .build();


        Mockito.when(this.transactionPort.findById(TRANSACTION_ID)).thenReturn(Mono.just(transaction));
        Mockito.when(this.transactionPort.save(Mockito.any())).then((Answer<Mono<Transaction>>) mock -> {
            Transaction copy = mock.getArgument(0);
            return Mono.just(Transaction.builder()
                    .id(copy.getId() == null ? "test" : copy.getId())
                    .description(copy.getDescription())
                    .origin(copy.getOrigin())
                    .commission(copy.getCommission())
                    .value(copy.getValue())
                    .date(copy.getDate())
                    .state(copy.getState())
                    .userId(copy.getUserId())
                    .fromUserId(copy.getFromUserId())
                    .build());
        });

        double commission = transaction.getValue() * 0.05;
        Mockito.when(this.userPort.sumBalanceById(SOURCE_USER_ID, transaction.getValue())).thenReturn(Mono.just(true));
        Mockito.when(this.bankTransactionCommissionUseCase.createBankTransaction(
                Mockito.eq(SOURCE_USER_ID),
                Mockito.any(),
                Mockito.eq(commission))).thenReturn(Mono.just(Transaction.builder().build()));

        Mono<Transaction> reply = this.userCancelTransactionUserCase.cancelTransaction(new UserCancelTransactionRequest(
                TRANSACTION_ID
        ));

        StepVerifier
                .create(reply)
                .expectNextMatches(ts -> ts.getState() == TransactionState.CANCELLED)
                .verifyComplete();

    }

}
