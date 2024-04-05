package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.constants.Constants;
import com.pragmaticos.transactions.domain.exceptions.UserDailyLimitExceededException;
import com.pragmaticos.transactions.domain.exceptions.UserInsufficientBalanceException;
import com.pragmaticos.transactions.domain.exceptions.UserInsufficientBalanceForCommissionException;
import com.pragmaticos.transactions.domain.exceptions.UserNotExistsException;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.model.requests.UserCreateTransactionRequest;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import com.pragmaticos.transactions.infrastructure.configuration.logger.LoggerAdvice;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@RequiredArgsConstructor
public class UserCreateTransactionUseCaseImpl implements UserCreateTransactionUseCase {

    private final Logger LOGGER = LoggerFactory.getLogger(LoggerAdvice.class);

    private final UserPort userPort;
    private final TransactionPort transactionPort;
    private final BankTransactionCommissionUseCase bankTransactionCommissionUseCase;

    private Mono<Transaction> processTransactionCommission(Transaction transaction) {

        if (transaction.getCommission() <= 0) {
            return Mono.just(transaction);
        }

        return Mono.just(transaction)
                .flatMap(ts -> this.userPort.getById(ts.getFromUserId()))

                // Validamos de nuevo si tiene el dinero suficiente para la transacción.
                .filter(user -> user.getBalance() >= transaction.getValue() + transaction.getCommission())
                .switchIfEmpty(Mono.error(new UserInsufficientBalanceForCommissionException(transaction.getFromUserId(), transaction, "¡No hay dinero disponible en la cuenta para pagar la comisión de $" + transaction.getCommission() + "!")))

                // Ejecutamos el guardado del banco
                .then(bankTransactionCommissionUseCase.createBankTransaction(transaction.getFromUserId(),
                        "Comisión por transferencia entre usuarios.",
                        transaction.getCommission()))

                // Regresamos
                .thenReturn(transaction);


    }

    @Override
    public Mono<Transaction> createTransaction(UserCreateTransactionRequest data) {
        return userPort

                // 1. Verificar si existe el usuario que va enviar el dinero.
                .existsById(data.getToUserId())
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new UserNotExistsException(data.getToUserId(), "¡El usuario destinatario " + data.getToUserId() + " no existe!")))
                .doOnError(x -> {
                    LOGGER.info("UserCreateTransaction: No se ha logrado encontrar el usuario destinario " + data.getUserId());
                })

                // 2. Validar si el usuario que recibirá el dinero existe.
                .then(this.userPort.getById(data.getUserId()))
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new UserNotExistsException(data.getUserId(), "¡El usuario que quiere enviar el dinero " + data.getUserId() + " no existe!")))
                .doOnError(x -> {
                    LOGGER.info("UserCreateTransaction: No se ha logrado encontrar el usuario que desea enviar el dinero " + data.getUserId());
                })

                // 3. Validar si tiene el balance correspondiente.
                .filter(user -> user.getBalance() >= data.getValue())
                .switchIfEmpty(Mono.error(new UserInsufficientBalanceException(data.getUserId(), "¡No hay dinero disponible en la cuenta!")))

                // 4. Validar si no ha completado su limite de dinero diario
                .zipWith(this.transactionPort.dailySumOfTransactions(data.getUserId()))
                .filter(tuple -> tuple.getT2() < Constants.LIMIT_PER_DAY)
                .switchIfEmpty(Mono.error(new UserDailyLimitExceededException(data.getUserId(), "¡No se puede enviar más de " + Constants.LIMIT_PER_DAY + " por día!")))

                .flatMap(tuple -> {

                    double nextDailySum = tuple.getT2() + data.getValue();
                    double comission = 0;

                    if (nextDailySum > Constants.LIMIT_OF_COMMISSION_FREE) {
                        if (tuple.getT2() > Constants.LIMIT_OF_COMMISSION_FREE) {
                            comission = data.getValue();
                        } else {
                            comission = nextDailySum - Constants.LIMIT_OF_COMMISSION_FREE;
                        }
                        comission *= Constants.COMMISSION_PERCENTAGE_FOR_EXCEEDED_LIMIT;
                    }

                    Transaction transaction = Transaction.builder()
                            .userId(data.getToUserId())
                            .fromUserId(data.getUserId())
                            .date(LocalDateTime.now())
                            .value(data.getValue())
                            .state(TransactionState.PENDING)
                            .commission(comission)
                            .description("Transferencia entre Usuarios")
                            .origin(TransactionOrigin.TRANSFER_BETWEEN_ACCOUNT)
                            .build();

                    return this.transactionPort.save(transaction);

                })
                .flatMap(ts -> this.userPort.sumBalanceById(ts.getFromUserId(), -1 * ts.getValue()).thenReturn(ts))
                .flatMap(this::processTransactionCommission)
                .flatMap(ts -> this.userPort.sumBalanceById(ts.getUserId(), ts.getValue()).thenReturn(ts))
                .flatMap(ts -> {
                    ts.setState(TransactionState.COMPLETE);
                    return this.transactionPort.save(ts);
                });
    }

}
