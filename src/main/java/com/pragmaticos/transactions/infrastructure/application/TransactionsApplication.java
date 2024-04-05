package com.pragmaticos.transactions.infrastructure.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.List;
import java.util.regex.Pattern;

@SpringBootApplication(scanBasePackages = "com.pragmaticos.transactions")
@EnableAspectJAutoProxy
public class TransactionsApplication {

    public static final String USECASES_ROUTE = "com.pragmaticos.transactions.domain.usecases";
    public static final String ADAPTERS_ROUTES = "com.pragmaticos.transactions.adapters";
    public static final List<Pattern> EXCLUDE_ADAPTERS_ROUTES = List.of(
            Pattern.compile(".*\\.dtos\\..*"),
            Pattern.compile(".*\\.responses\\..*")
    );

    public static void main(String[] args) {
        SpringApplication.run(TransactionsApplication.class, args);
    }

}
