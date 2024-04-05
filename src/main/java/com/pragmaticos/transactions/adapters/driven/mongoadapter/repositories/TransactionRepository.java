package com.pragmaticos.transactions.adapters.driven.mongoadapter.repositories;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.TransactionEntity;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, String> {

    Flux<TransactionEntity> findAllByUserIdOrFromUserId(String userId, String fromUserId);

    Flux<TransactionEntity> findAllByFromUserIdAndOriginIsNotAndDateBetween(String fromUserId,
                                                                            TransactionOrigin origin,
                                                                            LocalDateTime startDate,
                                                                            LocalDateTime endDate);

}
