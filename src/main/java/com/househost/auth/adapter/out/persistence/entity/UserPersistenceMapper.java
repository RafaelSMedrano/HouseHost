package com.househost.auth.adapter.out.persistence.entity;

import com.househost.auth.domain.model.User;

public final class UserPersistenceMapper {
    private UserPersistenceMapper() {}

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        User user = new User(entity.username, entity.email, entity.passwordHash, entity.role, entity.photoUrl);
        user.restoreId(entity.id);
        user.updateProfile(entity.username, entity.email, entity.phone, entity.role);
        return user;
    }

    public static UserJpaEntity toEntity(User user) {
        if (user == null) return null;
        UserJpaEntity entity = new UserJpaEntity();
        entity.id = user.getId();
        entity.username = user.getUsername();
        entity.email = user.getEmail();
        entity.phone = user.getPhone();
        entity.passwordHash = user.getPasswordHash();
        entity.role = user.getRole();
        entity.photoUrl = user.getPhotoUrl();
        return entity;
    }
}
