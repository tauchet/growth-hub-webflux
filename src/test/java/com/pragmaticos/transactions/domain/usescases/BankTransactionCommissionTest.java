package com.pragmaticos.transactions.domain.usescases;

import com.pragmaticos.transactions.domain.exceptions.UserNotExistsException;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.model.User;
import com.pragmaticos.transactions.domain.model.requests.UserCreateTransactionRequest;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import com.pragmaticos.transactions.domain.usecases.BankTransactionCommissionUseCaseImpl;
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

import java.util.Objects;

@DisplayName("Enviar comisión al banco.")
@ExtendWith(MockitoExtension.class)
public class BankTransactionCommissionTest {

    @Mock
    TransactionPort transactionPort;

    @Mock
    UserPort userPort;

    @InjectMocks
    BankTransactionCommissionUseCaseImpl bankTransactionCommissionUseCase;

    @Test
    @DisplayName("El usuario que envia no existe.")
    public void onUserNotExistsError() {

        // Destination
        final String SOURCE_USER_ID = "123";
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.empty());
        Mockito.when(this.transactionPort.save(Mockito.any())).thenReturn(Mono.empty());
        Mono<Transaction> reply = this.bankTransactionCommissionUseCase.createBankTransaction(
                SOURCE_USER_ID,
                "Prueba de Comisión",
                10_000D
        );

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof UserNotExistsException && ((UserNotExistsException) e).getUserId().equals(SOURCE_USER_ID))
                .verify();

    }

    @Test
    @DisplayName("El usuario paga la comisión.")
    public void onUserInsufficientBalanceError() {

        // Destination
        final String SOURCE_USER_ID = "123";
        final String BANK_ID = "1";

        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.just(User.builder()
                .id(SOURCE_USER_ID)
                .name("source")
                .username("source".toLowerCase())
                .balance(9_000_000D)
                .build()));
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

        Mockito.when(this.userPort.sumBalanceById(BANK_ID, 10_000D)).thenReturn(Mono.just(true));
        Mockito.when(this.userPort.sumBalanceById(SOURCE_USER_ID, -10_000D)).thenReturn(Mono.just(true));

        Mono<Transaction> reply = this.bankTransactionCommissionUseCase.createBankTransaction(
                SOURCE_USER_ID,
                "Prueba de Comisión",
                10_000D
        );

        StepVerifier
                .create(reply)
                .expectNextMatches(ts -> ts.getValue() == 10_000D &&
                        ts.getCommission() == 0D &&
                        ts.getOrigin() == TransactionOrigin.BANK_TRANSFER &&
                        Objects.equals(ts.getFromUserId(), SOURCE_USER_ID) &&
                        Objects.equals(ts.getUserId(), BANK_ID) &&
                        ts.getState() == TransactionState.COMPLETE)
                .verifyComplete();

    }

}
