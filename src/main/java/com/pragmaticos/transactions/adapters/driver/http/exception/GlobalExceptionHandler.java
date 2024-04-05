package com.pragmaticos.transactions.adapters.driver.http.exception;

import com.pragmaticos.transactions.adapters.driver.http.responses.ErrorResponse;
import com.pragmaticos.transactions.domain.exceptions.*;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties webProperties,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::formatErrorResponse);
    }

    private static final Map<Class<? extends RuntimeException>, String> ERROR_TYPES_NAMES = Map.ofEntries(
            Map.entry(UserNotExistsException.class, "USER_NOT_EXISTS"),
            Map.entry(TransactionNotExistsException.class, "TRANSACTION_NOT_EXISTS")
    );

    private static final Map<Class<? extends RuntimeException>, HttpStatus> ERROR_CODES = Map.ofEntries(
            Map.entry(UserNotExistsException.class, HttpStatus.NOT_FOUND),
            Map.entry(TransactionNotExistsException.class, HttpStatus.NOT_FOUND)
    );

    private Mono<ServerResponse> formatErrorResponse(ServerRequest request) {
        Throwable exception = getError(request);
        int httpCode = ERROR_CODES.getOrDefault(exception.getClass(), HttpStatus.INTERNAL_SERVER_ERROR).value();
        String name = ERROR_TYPES_NAMES.getOrDefault(exception.getClass(), "UNKNOWN");
        Map<String, Object> details = new HashMap<>();

        if (exception instanceof UserException) {
            details.put("userId", ((UserException) exception).getUserId());
        }

        if (exception instanceof TransactionException) {
            details.put("transactionId", ((TransactionException) exception).getTransactionId());
        }

        return ServerResponse
                .status(httpCode)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponse(httpCode, name, exception.getMessage(), details));
    }

}
