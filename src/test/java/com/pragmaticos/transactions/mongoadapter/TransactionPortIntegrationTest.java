package com.pragmaticos.transactions.mongoadapter;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.config.MongoAdapter;
import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.TransactionEntity;
import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.UserEntity;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.infrastructure.application.TransactionsApplication;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Objects;

@SpringBootTest(classes = {TransactionsApplication.class, MongoAdapter.class})
@SpringBootConfiguration
@DisplayName("Integración de puerto de transacciones.")
public class TransactionPortIntegrationTest {

    @Autowired
    TransactionPort transactionPort;

    @Autowired
    ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        this.mongoTemplate.dropCollection(UserEntity.class).block();
        this.mongoTemplate.dropCollection(TransactionEntity.class).block();
    }

    @Test
    @DisplayName("Guardar una transacción.")
    public void saveTransaction() {

        Mono<Transaction> reply = this.transactionPort.save(Transaction.builder()
                .value(1_000_000)
                .date(LocalDateTime.now())
                .id("test2")
                .state(TransactionState.PENDING)
                .commission(20_000D)
                .userId("123")
                .fromUserId("345")
                .build());

        StepVerifier
                .create(reply)
                .expectNextMatches(ts -> Objects.equals(ts.getId(), "test2") &&
                        ts.getState() == TransactionState.PENDING &&
                        ts.getCommission() == 20_000D &&
                        Objects.equals(ts.getUserId(), "123") &&
                        Objects.equals(ts.getFromUserId(), "345"))
                .verifyComplete();


    }

    @Test
    @DisplayName("Buscar una transacción que no existe.")
    public void findTransactionNotExists() {

        Mono<Transaction> reply = this.transactionPort.findById("test");

        StepVerifier
                .create(reply)
                .verifyComplete();


    }

    @Test
    @DisplayName("Buscar una transacción que exista.")
    public void findTransactionExists() {

        Mono<Transaction> save = this.transactionPort.save(Transaction.builder()
                .value(1_000_000)
                .date(LocalDateTime.now())
                .id("test2")
                .state(TransactionState.PENDING)
                .commission(20_000D)
                .userId("123")
                .fromUserId("345")
                .build());

        Mono<Transaction> reply = save.flatMap(x -> this.transactionPort.findById("test2"));
        StepVerifier
                .create(reply)
                .expectNextMatches(ts -> Objects.equals(ts.getId(), "test2") &&
                        ts.getState() == TransactionState.PENDING &&
                        ts.getCommission() == 20_000D &&
                        Objects.equals(ts.getUserId(), "123") &&
                        Objects.equals(ts.getFromUserId(), "345"))
                .verifyComplete();

    }


    @Test
    @DisplayName("Sumatoria de transacciones de un día sin contar los demás días.")
    public void dailySumOfTransactions() {

        Mono<Double> reply = this.transactionPort.save(createTransaction(LocalDateTime.now()))
                .then(this.transactionPort.save(createTransaction(LocalDateTime.now())))
                .then(this.transactionPort.save(createTransaction(LocalDateTime.now())))
                .then(this.transactionPort.save(createTransaction(LocalDateTime.now())))
                .then(this.transactionPort.save(createTransaction(LocalDateTime.now().minusDays(1))))
                .then(this.transactionPort.dailySumOfTransactions("345"));

        StepVerifier
                .create(reply)
                .expectNextMatches(ts -> {
                    System.out.println("Reply: " + ts);
                    return ts == 4_000_000;
                })
                .verifyComplete();

    }

    private Transaction createTransaction(LocalDateTime date) {

        Transaction ts = Transaction.builder()
                .value(1_000_000)
                .date(date)
                .origin(TransactionOrigin.TRANSFER_BETWEEN_ACCOUNT)
                .state(TransactionState.COMPLETE)
                .commission(20_000D)
                .userId("123")
                .fromUserId("345")
                .build();

        System.out.println("Creo: " + ts);

        return ts;
    }

}
