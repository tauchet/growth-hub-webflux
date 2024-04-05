package com.pragmaticos.transactions.adapters.driven.mongoadapter.repositories;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.TransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, String> {

    Flux<TransactionEntity> findAllByUserIdOrFromUserId(String userId, String fromUserId);
}
