package com.pragmaticos.transactions.adapters.driver.http.responses;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorResponse {

    private final int status;
    private final String type;
    private final String description;
    private final Map<String, Object> details;

    public ErrorResponse(int status, String type, String description, Map<String, Object> details) {
        this.status = status;
        this.type = type;
        this.description = description;
        this.details = details;
    }


}
