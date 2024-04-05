package com.pragmaticos.transactions.adapters.driver.http.router.handlers;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.TransactionEntity;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.requests.UserCancelTransactionRequest;
import com.pragmaticos.transactions.domain.model.requests.UserCreateTransactionRequest;
import com.pragmaticos.transactions.domain.usecases.TransactionsByUserUseCase;
import com.pragmaticos.transactions.domain.usecases.UserCancelTransactionUserCase;
import com.pragmaticos.transactions.domain.usecases.UserCreateTransactionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TransactionsHandler {

    private final TransactionsByUserUseCase transactionsByUserUseCase;
    private final UserCreateTransactionUseCase createTransactionUseCase;
    private final UserCancelTransactionUserCase cancelTransactionUserCase;

    public Mono<ServerResponse> getAllByUserId(ServerRequest request) {
        String userId = request.pathVariable("id");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.transactionsByUserUseCase.findAllByUserId(userId), Transaction.class);
    }

    public Mono<ServerResponse> createTransaction(ServerRequest request) {
        return request
                .bodyToMono(UserCreateTransactionRequest.class)
                .switchIfEmpty(Mono.error(new RuntimeException("¡No se ha encontrado el contenido de la petición!")))
                .flatMap(this.createTransactionUseCase::createTransaction)
                .flatMap(r -> {
                    System.out.println(r);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(r);
                });
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
