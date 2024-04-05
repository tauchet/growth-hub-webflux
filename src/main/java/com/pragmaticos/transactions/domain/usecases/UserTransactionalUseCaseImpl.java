package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.exceptions.UserNotExistsException;
import com.pragmaticos.transactions.domain.model.User;
import com.pragmaticos.transactions.domain.ports.UserPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RequiredArgsConstructor
public class UserTransactionalUseCaseImpl implements UserTransactionalUseCase {

    private final UserPort userPort;

    @Override
    public Mono<User> getById(String id) {
        return this.userPort
                .getById(id)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new UserNotExistsException(id, "¡El usuario no existe!")));
    }

}
