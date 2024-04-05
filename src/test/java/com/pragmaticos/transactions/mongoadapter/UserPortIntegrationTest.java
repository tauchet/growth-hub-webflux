package com.pragmaticos.transactions.mongoadapter;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.config.MongoAdapter;
import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.TransactionEntity;
import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.UserEntity;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.model.User;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import com.pragmaticos.transactions.infrastructure.application.TransactionsApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Objects;

@SpringBootTest(classes = {TransactionsApplication.class, MongoAdapter.class})
@SpringBootConfiguration
@DisplayName("Integración de puerto de usuarios.")
public class UserPortIntegrationTest {

    @Autowired
    UserPort userPort;

    @Autowired
    ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        this.mongoTemplate.dropCollection(UserEntity.class).block();
        this.mongoTemplate.dropCollection(TransactionEntity.class).block();
    }

    @Test
    @DisplayName("Guardar un usuario.")
    public void saveTransaction() {


        Mono<User> reply = this.userPort.save(
                User.builder()
                        .id("test")
                        .name("tauchet")
                        .username("tauchet")
                        .balance(0D)
                        .build()
        );

        StepVerifier
                .create(reply)
                .expectNextMatches(u -> Objects.equals(u.getId(), "test") &&
                        Objects.equals(u.getName(), "tauchet") &&
                        Objects.equals(u.getUsername(), "tauchet") &&
                        Objects.equals(u.getBalance(), 0D))
                .verifyComplete();


    }

    @Test
    @DisplayName("Buscar una usuario que no existe.")
    public void findTransactionNotExists() {
        Mono<User> reply = this.userPort.getById("123");
        StepVerifier
                .create(reply)
                .verifyComplete();
    }

    @Test
    @DisplayName("Validar que un usuario que no existe.")
    public void checkTransactionNotExists() {
        Mono<Boolean> reply = this.userPort.existsById("123");
        StepVerifier
                .create(reply)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Buscar un usuario que exista.")
    public void findUserExists() {

        Mono<User> save = this.userPort.save(
                User.builder()
                        .id("test2")
                        .name("tauchet")
                        .username("tauchet")
                        .balance(0D)
                        .build()
        );

        Mono<User> reply = save.flatMap(x -> this.userPort.getById("test2"));
        StepVerifier
                .create(reply)
                .expectNextMatches(u -> Objects.equals(u.getId(), "test2") &&
                        Objects.equals(u.getName(), "tauchet") &&
                        Objects.equals(u.getUsername(), "tauchet") &&
                        Objects.equals(u.getBalance(), 0D))
                .verifyComplete();

    }

    @Test
    @DisplayName("Validar que un usuario que exista.")
    public void checkUserExists() {

        Mono<User> save = this.userPort.save(
                User.builder()
                        .id("test2")
                        .name("tauchet")
                        .username("tauchet")
                        .balance(0D)
                        .build()
        );

        Mono<Boolean> reply = save.flatMap(x -> this.userPort.existsById("test2"));
        StepVerifier
                .create(reply)
                .expectNext(true)
                .verifyComplete();

    }


    @Test
    @DisplayName("Agregar dinero a la cuenta del usuario.")
    public void addBalanceToUser() {

        Mono<User> save = this.userPort.save(User.builder()
                        .id("test2")
                        .name("tauchet")
                        .username("tauchet")
                        .balance(0D)
                        .build()
        );

        Mono<Boolean> update = save.flatMap(x -> this.userPort.sumBalanceById(x.getId(), 2000D));
        StepVerifier
                .create(update)
                .expectNext(true)
                .verifyComplete();

        Mono<User> reply = this.userPort.getById("test2");
        StepVerifier
                .create(reply)
                .expectNextMatches(u -> u.getBalance() == 2000D)
                .verifyComplete();

    }

    @Test
    @DisplayName("Retirar dinero a la cuenta del usuario.")
    public void removeBalanceToUser() {

        Mono<User> save = this.userPort.save(User.builder()
                .id("test2")
                .name("tauchet")
                .username("tauchet")
                .balance(10_000D)
                .build()
        );

        Mono<Boolean> update = save.flatMap(x -> this.userPort.sumBalanceById(x.getId(), -2000D));
        StepVerifier
                .create(update)
                .expectNext(true)
                .verifyComplete();

        Mono<User> reply = this.userPort.getById("test2");
        StepVerifier
                .create(reply)
                .expectNextMatches(u -> u.getBalance() == 8000D)
                .verifyComplete();

    }


}
