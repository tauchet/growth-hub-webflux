package com.pragmaticos.transactions.domain.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class User {

    private String id;
    private String username;
    private String name;
    private double balance;

}
