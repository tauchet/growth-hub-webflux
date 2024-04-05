package com.pragmaticos.transactions.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class Transaction {

    private String id;
    private String userId;
    private String fromUserId;
    private double value;
    private LocalDateTime date;
    private TransactionState state;
    private TransactionOrigin origin;
    private double commission;
    private String description;


}
