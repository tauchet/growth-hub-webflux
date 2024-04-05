package com.pragmaticos.transactions.adapters.driver.http.router.handlers;

import com.pragmaticos.transactions.domain.model.requests.UserCancelTransactionRequest;
import com.pragmaticos.transactions.domain.model.requests.UserCreateTransactionRequest;
import com.pragmaticos.transactions.domain.usecases.TransactionsByUserUseCase;
import com.pragmaticos.transactions.domain.usecases.UserCancelTransactionUserCase;
import com.pragmaticos.transactions.domain.usecases.UserCreateTransactionUseCase;
import com.pragmaticos.transactions.domain.usecases.UserTransactionalUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TransactionsHandler {

    private final UserCreateTransactionUseCase createTransactionUseCase;
    private final TransactionsByUserUseCase transactionsByUserUseCase;
    private final UserTransactionalUseCase userTransactionalUseCase;
    private final UserCancelTransactionUserCase cancelTransactionUserCase;

    public Mono<ServerResponse> getUserById(ServerRequest request) {
        String userId = request.pathVariable("id");
        return this.userTransactionalUseCase
                .getById(userId)
                .flatMap(user -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(user));
    }

    public Mono<ServerResponse> getAllByUserId(ServerRequest request) {
        String userId = request.pathVariable("id");
        return this.transactionsByUserUseCase
                .findAllByUserId(userId)
                .collectList()
                .flatMap(list -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list));
    }

    public Mono<ServerResponse> createTransaction(ServerRequest request) {
        return request
                .bodyToMono(UserCreateTransactionRequest.class)
                .switchIfEmpty(Mono.error(new RuntimeException("¡No se ha encontrado el contenido de la petición!")))
                .flatMap(this.createTransactionUseCase::createTransaction)
                .flatMap(r -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(r));
    }


    public Mono<ServerResponse> cancelTransaction(ServerRequest request) {
        return request
                .bodyToMono(UserCancelTransactionRequest.class)
                .flatMap(this.cancelTransactionUserCase::cancelTransaction)
                .flatMap(r -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(r));
    }


}
