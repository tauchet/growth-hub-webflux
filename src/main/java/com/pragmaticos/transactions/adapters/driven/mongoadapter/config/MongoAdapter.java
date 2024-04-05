package com.pragmaticos.transactions.adapters.driven.mongoadapter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.pragmaticos.transactions.adapters.driven.mongoadapter")
@EnableMongoRepositories(basePackages = "com.pragmaticos.transactions.adapters.driven.mongoadapter")
@EntityScan(basePackages = "com.pragmaticos.transactions.adapters.driven.mongoadapter")
@RequiredArgsConstructor
public class MongoAdapter {
}
