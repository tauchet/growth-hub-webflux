package com.pragmaticos.transactions.domain.usescases;

import com.pragmaticos.transactions.domain.exceptions.UserDailyLimitExceededException;
import com.pragmaticos.transactions.domain.exceptions.UserInsufficientBalanceException;
import com.pragmaticos.transactions.domain.exceptions.UserInsufficientBalanceForCommissionException;
import com.pragmaticos.transactions.domain.exceptions.UserNotExistsException;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.model.User;
import com.pragmaticos.transactions.domain.model.requests.UserCreateTransactionRequest;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import com.pragmaticos.transactions.domain.usecases.*;
import org.junit.jupiter.api.BeforeEach;
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

@DisplayName("Crear una transacción.")
@ExtendWith(MockitoExtension.class)
public class UserCreateTransactionTest {

    @Mock
    TransactionPort transactionPort;

    @Mock
    UserPort userPort;

    @Mock
    BankTransactionCommissionUseCase bankTransactionCommissionUseCase;

    @InjectMocks
    UserCreateTransactionUseCaseImpl userCreateTransactionUseCase;

    String SOURCE_USER_ID = "123";
    String DESTINATION_USER_ID = "345";

    @Test
    @DisplayName("El usuario destinatario no existe.")
    public void onDestinationNotExistsError() {

        // Destination
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.empty());
        Mockito.when(this.userPort.existsById(DESTINATION_USER_ID)).thenReturn(Mono.just(false));
        Mockito.when(this.transactionPort.dailySumOfTransactions(SOURCE_USER_ID)).thenReturn(Mono.just(0D));

        Mono<Transaction> reply = this.userCreateTransactionUseCase.createTransaction(new UserCreateTransactionRequest(
                SOURCE_USER_ID,
                DESTINATION_USER_ID,
                1000
        ));

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof UserNotExistsException && ((UserNotExistsException) e).getUserId().equals(DESTINATION_USER_ID))
                .verify();

    }

    @Test
    @DisplayName("El usuario que envia no existe.")
    public void onSourceNotExistsError() {

        // Destination
        Mockito.when(this.userPort.existsById(DESTINATION_USER_ID)).thenReturn(Mono.just(true));
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.empty());
        Mockito.when(this.transactionPort.dailySumOfTransactions(SOURCE_USER_ID)).thenReturn(Mono.just(0D));

        Mono<Transaction> reply = this.userCreateTransactionUseCase.createTransaction(new UserCreateTransactionRequest(
                SOURCE_USER_ID,
                DESTINATION_USER_ID,
                1000
        ));

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof UserNotExistsException && ((UserNotExistsException) e).getUserId().equals(SOURCE_USER_ID))
                .verify();

    }

    @Test
    @DisplayName("El usuario que envia excede el límite de dinero diario.")
    public void onInsufficientBalanceError() {

        // Destination
        Mockito.when(this.userPort.existsById(DESTINATION_USER_ID)).thenReturn(Mono.just(true));
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.just(createUserSource(0D)));
        Mockito.when(this.transactionPort.dailySumOfTransactions(SOURCE_USER_ID)).thenReturn(Mono.just(0D));

        Mono<Transaction> reply = this.userCreateTransactionUseCase.createTransaction(new UserCreateTransactionRequest(
                SOURCE_USER_ID,
                DESTINATION_USER_ID,
                1000
        ));

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof UserInsufficientBalanceException && ((UserInsufficientBalanceException) e).getUserId().equals(SOURCE_USER_ID))
                .verify();

    }

    @Test
    @DisplayName("El usuario que envia no tiene suficiente dinero.")
    public void onDailyLimitExceededError() {

        // Destination
        Mockito.when(this.userPort.existsById(DESTINATION_USER_ID)).thenReturn(Mono.just(true));
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.just(createUserSource(1_000_000D)));
        Mockito.when(this.transactionPort.dailySumOfTransactions(SOURCE_USER_ID)).thenReturn(Mono.just(10_000_000D));

        Mono<Transaction> reply = this.userCreateTransactionUseCase.createTransaction(new UserCreateTransactionRequest(
                SOURCE_USER_ID,
                DESTINATION_USER_ID,
                1000
        ));

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof UserDailyLimitExceededException && ((UserDailyLimitExceededException) e).getUserId().equals(SOURCE_USER_ID))
                .verify();

    }

    @Test
    @DisplayName("El usuario que envia no tiene suficiente dinero para pagar la comisión y la transacción queda pendiente.")
    public void onDailyLimitExceededWithCommissionError() {

        // Destination
        Mockito.when(this.userPort.existsById(DESTINATION_USER_ID)).thenReturn(Mono.just(true));
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.just(createUserSource(9_000_000)));
        Mockito.when(this.transactionPort.dailySumOfTransactions(SOURCE_USER_ID)).thenReturn(Mono.just(0D));
        Mockito.when(this.userPort.sumBalanceById(Mockito.anyString(), Mockito.anyDouble())).thenReturn(Mono.just(true));
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

        Mockito.when(this.bankTransactionCommissionUseCase.createBankTransaction(
                Mockito.eq(SOURCE_USER_ID),
                Mockito.anyString(),
                Mockito.eq(80000D))).thenReturn(Mono.just(Transaction.builder().build()));

        Mono<Transaction> reply = this.userCreateTransactionUseCase.createTransaction(new UserCreateTransactionRequest(
                SOURCE_USER_ID,
                DESTINATION_USER_ID,
                9_000_000
        ));

        StepVerifier
                .create(reply)
                .expectErrorMatches(e -> e instanceof UserInsufficientBalanceForCommissionException &&
                        ((UserInsufficientBalanceForCommissionException) e).getUserId().equals(SOURCE_USER_ID) &&
                        ((UserInsufficientBalanceForCommissionException) e).getTransaction().getState() == TransactionState.PENDING)
                .verify();

    }

    @Test
    @DisplayName("El usuario desea transferir por exceder su límite de dinero diario gratuito. ($5.500.000)")
    public void onDailyLimitFreeWhenFirstTransfer() {

        // Destination
        Mockito.when(this.userPort.existsById(DESTINATION_USER_ID)).thenReturn(Mono.just(true));
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.just(createUserSource(9_000_000)));
        Mockito.when(this.transactionPort.dailySumOfTransactions(SOURCE_USER_ID)).thenReturn(Mono.just(0D));
        Mockito.when(this.userPort.sumBalanceById(Mockito.anyString(), Mockito.anyDouble())).thenReturn(Mono.just(true));
        Mockito.when(this.bankTransactionCommissionUseCase.createBankTransaction(
                Mockito.eq(SOURCE_USER_ID),
                Mockito.any(),
                Mockito.anyDouble())).thenReturn(Mono.just(Transaction.builder().build()));

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

        Mono<Transaction> reply = this.userCreateTransactionUseCase.createTransaction(new UserCreateTransactionRequest(
                SOURCE_USER_ID,
                DESTINATION_USER_ID,
                5_500_000
        ));

        StepVerifier
                .create(reply)
                .expectNextMatches(transaction -> transaction.getCommission() == 10000)
                .verifyComplete();

    }

    @Test
    @DisplayName("El usuario desea transferir $2.000.000 una vez ya transfirió $5.500.000 en el día.")
    public void onDailyLimitFreeWhenSecondTransfer() {

        // Destination
        Mockito.when(this.userPort.existsById(DESTINATION_USER_ID)).thenReturn(Mono.just(true));
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.just(createUserSource(9_000_000)));
        Mockito.when(this.transactionPort.dailySumOfTransactions(SOURCE_USER_ID)).thenReturn(Mono.just(5_500_000D));
        Mockito.when(this.userPort.sumBalanceById(Mockito.anyString(), Mockito.anyDouble())).thenReturn(Mono.just(true));
        Mockito.when(this.bankTransactionCommissionUseCase.createBankTransaction(
                Mockito.eq(SOURCE_USER_ID),
                Mockito.anyString(),
                Mockito.eq(40000D))).thenReturn(Mono.just(Transaction.builder().build()));

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

        Mono<Transaction> reply = this.userCreateTransactionUseCase.createTransaction(new UserCreateTransactionRequest(
                SOURCE_USER_ID,
                DESTINATION_USER_ID,
                2_000_000
        ));

        StepVerifier
                .create(reply)
                .expectNextMatches(transaction -> transaction.getCommission() == 40000)
                .verifyComplete();

    }

    @Test
    @DisplayName("El usuario desea transferir dinero completamente.")
    public void onTransactionSuccess() {

        // Destination
        Mockito.when(this.userPort.existsById(DESTINATION_USER_ID)).thenReturn(Mono.just(true));
        Mockito.when(this.userPort.getById(SOURCE_USER_ID)).thenReturn(Mono.just(createUserSource(9_000_000)));
        Mockito.when(this.transactionPort.dailySumOfTransactions(SOURCE_USER_ID)).thenReturn(Mono.just(0D));
        Mockito.when(this.userPort.sumBalanceById(Mockito.anyString(), Mockito.anyDouble())).thenReturn(Mono.just(true));
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

        Mono<Transaction> reply = this.userCreateTransactionUseCase.createTransaction(new UserCreateTransactionRequest(
                SOURCE_USER_ID,
                DESTINATION_USER_ID,
                100_000
        ));

        StepVerifier
                .create(reply)
                .expectNextMatches(ts -> ts.getState() == TransactionState.COMPLETE && ts.getValue() == 100_000 && ts.getCommission() == 0)
                .verifyComplete();

    }

    private User createUserSource(double balance) {
        return User.builder()
                .id(SOURCE_USER_ID)
                .name("source")
                .username("source".toLowerCase())
                .balance(balance)
                .build();
    }

    private User createUserDestination(double balance) {
        return User.builder()
                .id(DESTINATION_USER_ID)
                .name("destination")
                .username("destination".toLowerCase())
                .balance(balance)
                .build();
    }

}
