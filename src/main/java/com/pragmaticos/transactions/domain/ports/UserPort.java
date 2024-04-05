package com.pragmaticos.transactions.domain.ports;

import com.mongodb.client.result.UpdateResult;
import com.pragmaticos.transactions.domain.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserPort {

    Mono<Boolean> sumBalanceById(String id, double sum);

    Mono<Boolean> existsById(String id);

    Mono<User> getById(String id);

    Mono<User> save(User user);

}
