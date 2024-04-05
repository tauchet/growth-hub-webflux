package com.pragmaticos.transactions.adapters.driven.mongoadapter.service;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.UserEntity;
import com.pragmaticos.transactions.adapters.driven.mongoadapter.mapper.UserMapper;
import com.pragmaticos.transactions.adapters.driven.mongoadapter.repositories.UserRepository;
import com.pragmaticos.transactions.domain.model.User;
import com.pragmaticos.transactions.domain.ports.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RequiredArgsConstructor
public class UserService implements UserPort {

    private final ReactiveMongoTemplate template;
    private final UserRepository userRepository;

    @Override
    public Mono<Boolean> sumBalanceById(String id, double sum) {
        Query query = Query.query(where("_id").is(id));
        Update update = new Update().inc("balance", sum);
        return this.template
                .updateFirst(query, update, UserEntity.class)
                .map(x -> x.getModifiedCount() >= 1);
    }

    @Override
    public Mono<Boolean> existsById(String id) {
        return this.userRepository.existsById(id);
    }


    @Override
    public Mono<User> getById(String id) {
        return this.userRepository
                .findById(id)
                .map(UserMapper::mapToUser);
    }

    @Override
    public Mono<User> save(User userEntity) {
        return this.userRepository
                .save(UserMapper.mapToUserEntity(userEntity))
                .map(UserMapper::mapToUser);
    }

}
