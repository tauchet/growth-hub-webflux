package com.pragmaticos.transactions.adapters.driven.mongoadapter.repositories;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, String> {
}
