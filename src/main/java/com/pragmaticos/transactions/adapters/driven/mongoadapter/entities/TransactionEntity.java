package com.pragmaticos.transactions.adapters.driven.mongoadapter.entities;

import com.pragmaticos.transactions.domain.model.TransactionOrigin;
import com.pragmaticos.transactions.domain.model.TransactionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "transactions")
public class TransactionEntity {

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
