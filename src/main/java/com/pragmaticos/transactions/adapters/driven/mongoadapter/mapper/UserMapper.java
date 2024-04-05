package com.pragmaticos.transactions.adapters.driven.mongoadapter.mapper;

import com.pragmaticos.transactions.adapters.driven.mongoadapter.entities.UserEntity;
import com.pragmaticos.transactions.domain.model.User;

public class UserMapper {

    public static User mapToUser(UserEntity userEntity) {
        return User.builder()
                .username(userEntity.getUsername())
                .id(userEntity.getId())
                .balance(userEntity.getBalance())
                .name(userEntity.getName())
                .build();
    }

    public static UserEntity mapToUserEntity(User user) {
        return UserEntity.builder()
                .username(user.getUsername())
                .id(user.getId())
                .balance(user.getBalance())
                .name(user.getName())
                .build();
    }

}
