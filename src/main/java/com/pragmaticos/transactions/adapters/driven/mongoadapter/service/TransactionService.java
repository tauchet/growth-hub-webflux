package com.pragmaticos.transactions.adapters.driven.mongoadapter.service;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.TransactionEntity;
import com.pragmaticos.transactions.adapters.driven.mongoadapter.mapper.TransactionMapper;
import com.pragmaticos.transactions.adapters.driven.mongoadapter.repositories.TransactionRepository;
import com.pragmaticos.transactions.domain.model.Transaction;
import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import com.pragmaticos.transactions.domain.ports.TransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
public class TransactionService implements TransactionPort {

    private final ReactiveMongoTemplate template;
    private final TransactionRepository transactionRepository;

    @Override
    public Flux<Transaction> findAllByUserId(String userId) {
        Query query = query(new Criteria().orOperator(where("userId").is(userId), where("fromUserId").is(userId)));
        return this.template
                .find(query, TransactionEntity.class)
                .filter(Objects::nonNull)
                .map(TransactionMapper::mapToTransaction);
    }

    @Override
    public Mono<Double> dailySumOfTransactions(String userId) {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();

        System.out.println("Inicio: " + startOfDay);
        System.out.println("Final: " + endOfDay);

        Query query = query(new Criteria().andOperator(
                where("fromUserId").is(userId),
                where("origin").ne(TransactionOrigin.BANK_TRANSFER),
                where("date").gte(startOfDay).lt(endOfDay)
        ));
        return this.template
                .find(query, TransactionEntity.class)
                .reduce(0D, (count, x) -> {
                    System.out.println(x);
                    return count + x.getValue();
                });
    }

    @Override
    public Mono<Transaction> save(Transaction transaction) {
        return this.transactionRepository
                .save(TransactionMapper.mapToTransactionEntity(transaction))
                .map(TransactionMapper::mapToTransaction);
    }

    @Override
    public Mono<Transaction> findById(String id) {
        return this.transactionRepository
                .findById(id)
                .map(TransactionMapper::mapToTransaction);
    }


}
