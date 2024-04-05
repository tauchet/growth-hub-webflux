package com.pragmaticos.transactions.adapters.driver.http.router;

import com.pragmaticos.transactions.adapters.driver.http.router.handlers.TransactionsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class MainRouter {

    @Bean
    public RouterFunction<ServerResponse> initialRoutes(TransactionsHandler tsHandler) {
        return RouterFunctions
                .route(RequestPredicates.GET("/fun/users/{id}/transactions"), tsHandler::getAllByUserId)
                .andRoute(RequestPredicates.POST("/fun/create-transaction"), tsHandler::createTransaction)
                .andRoute(RequestPredicates.POST("/fun/cancel-transaction"), tsHandler::cancelTransaction);
    }


}

