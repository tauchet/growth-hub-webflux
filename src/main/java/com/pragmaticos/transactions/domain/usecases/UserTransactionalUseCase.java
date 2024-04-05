package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.model.User;
import reactor.core.publisher.Mono;

public interface UserTransactionalUseCase {

    Mono<User> getById(String id);

}
