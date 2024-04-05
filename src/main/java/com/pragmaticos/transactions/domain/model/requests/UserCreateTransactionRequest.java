package com.pragmaticos.transactions.domain.model.requests;

import lombok.Data;
@Data
public class UserCreateTransactionRequest {

    private final String userId;
    private final String toUserId;
    private final double value;

}
