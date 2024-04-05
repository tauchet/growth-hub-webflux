package com.pragmaticos.transactions.domain.usescases;

import com.pragmaticos.transactions.domain.exceptions.UserNotExistsException;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.model.User;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import com.pragmaticos.transactions.domain.usecases.BankTransactionCommissionUseCaseImpl;
import com.pragmaticos.transactions.domain.usecases.UserTransactionalUseCase;
import com.pragmaticos.transactions.domain.usecases.UserTransactionalUseCaseImpl;
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

@DisplayName("Funciones transacionales de un usuario.")
@ExtendWith(MockitoExtension.class)
public class UserTransactionalTest {

    @Mock
    UserPort userPort;

    @InjectMocks
    UserTransactionalUseCaseImpl userTransactionalUseCase;

    @Test
    @DisplayName("El usuario no existe.")
    public void onUserNotExistsError() {

        // Destination
        final String SOURCE_USER_ID = "123";
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.empty());
        Mono<User> reply = this.userTransactionalUseCase.getById(SOURCE_USER_ID);

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof UserNotExistsException && ((UserNotExistsException) e).getUserId().equals(SOURCE_USER_ID))
                .verify();

    }

    @Test
    @DisplayName("El usuario existe.")
    public void onUserSuccess() {

        // Destination
        final String SOURCE_USER_ID = "123";
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.just(User.builder()
                .id(SOURCE_USER_ID)
                .name("source")
                .username("source".toLowerCase())
                .balance(9_000_000D)
                .build()));
        Mono<User> reply = this.userTransactionalUseCase.getById(SOURCE_USER_ID);

        StepVerifier
                .create(reply)
                .expectNextMatches(u -> u.getId().equals(SOURCE_USER_ID) && u.getBalance() == 9_000_000D)
                .verifyComplete();

    }

}
