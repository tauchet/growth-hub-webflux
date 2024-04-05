package com.pragmaticos.transactions.domain.usecases;

import com.pragmaticos.transactions.domain.constants.Constants;
import com.pragmaticos.transactions.domain.exceptions.TransactionNotExistsException;
import com.pragmaticos.transactions.domain.exceptions.TransactionNotPendingException;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.model.requests.UserCancelTransactionRequest;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import com.pragmaticos.transactions.domain.ports.UserPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserCancelTransactionUserCaseImpl implements UserCancelTransactionUserCase {

    private final UserPort userPort;
    private final TransactionPort transactionPort;

    private final BankTransactionCommissionUseCaseImpl bankTransactionCommissionUseCase;

    @Override
    public Mono<Transaction> cancelTransaction(UserCancelTransactionRequest data) {

        return this.transactionPort

                // 1. Validar que exista la transacción
                .findById(data.getTransactionId())
                .switchIfEmpty(Mono.error(new TransactionNotExistsException(data.getTransactionId(), "¡No se ha encontrado la transacción con id " + data.getTransactionId() + "!")))

                // 2. Validar que la transacción se encuentre en estado pendiente
                .filter(ts -> ts.getState() == TransactionState.PENDING)
                .switchIfEmpty(Mono.error(new TransactionNotPendingException(data.getTransactionId(), "¡La transacción no se encuentra en estado pendiente!")))

                // 3. Cobrar comisión por cancelación.
                .flatMap(ts -> {
                    double commission = ts.getValue() * Constants.COMMISSION_PERCENTAGE_FOR_CANCEL_TRANSACTION;
                    return this.bankTransactionCommissionUseCase
                            .createBankTransaction(ts.getFromUserId(),
                                    "Comisión por cancelar la transacción",
                                    commission,
                                    false)
                            .thenReturn(ts).zipWith(Mono.just(commission));
                })

                // 4. Regresar el dinero al usuario
                .flatMap(tuple -> {
                    Transaction ts = tuple.getT1();
                    double value = ts.getValue() - tuple.getT2();
                    return this.userPort.sumBalanceById(ts.getFromUserId(), value).thenReturn(ts);
                })

                // 5. Colocar la transacción en Cancelado.
                .flatMap(ts -> {
                    ts.setState(TransactionState.CANCELLED);
                    return this.transactionPort.save(ts);
                });


    }

}
