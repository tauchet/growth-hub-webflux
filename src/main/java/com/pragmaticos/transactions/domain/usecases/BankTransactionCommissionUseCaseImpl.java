package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.exceptions.UserNotExistsException;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import com.pragmaticos.transactions.infrastructure.configuration.logger.LoggerAdvice;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
public class BankTransactionCommissionUseCaseImpl implements BankTransactionCommissionUseCase {

    private final Logger LOGGER = LoggerFactory.getLogger(LoggerAdvice.class);

    private final UserPort userPort;
    private final TransactionPort transactionPort;

    @Override
    public Mono<Transaction> createBankTransaction(String userId, String description, double commission) {
        return this.userPort.getById(userId)

                // 1. Verificar si el usuario existe.
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new UserNotExistsException(userId, "¡El usuario " + userId + " no existe!")))

                // 2. Creamos la transación para el historial.
                .zipWith(this.transactionPort.save(Transaction.builder()
                        .date(LocalDateTime.now())
                        .value(commission)
                        .userId("1")
                        .fromUserId(userId)
                        .description(description)
                        .origin(TransactionOrigin.BANK_TRANSFER)
                        .state(TransactionState.PENDING)
                        .build()))

                .map(Tuple2::getT2)
                .flatMap(ts -> this.userPort.sumBalanceById(ts.getFromUserId(), -1 * ts.getValue()).thenReturn(ts))
                .flatMap(ts -> this.userPort.sumBalanceById(ts.getUserId(), ts.getValue()).thenReturn(ts))
                .flatMap(ts -> {
                    ts.setState(TransactionState.COMPLETE);
                    return this.transactionPort.save(ts);
                });
    }

}
