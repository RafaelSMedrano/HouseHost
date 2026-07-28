package com.househost.auth.adapter.out.persistence.entity;

import com.househost.auth.domain.model.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false, unique = true) String username;
    @Column(nullable = false, unique = true) String email;
    String phone;
    @Column(nullable = false) String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false) UserRole role;
    @Lob @Column(name = "photo_url", columnDefinition = "longtext") String photoUrl;
}
