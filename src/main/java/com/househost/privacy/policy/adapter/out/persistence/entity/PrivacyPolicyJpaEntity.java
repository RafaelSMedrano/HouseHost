package com.househost.privacy.policy.adapter.out.persistence.entity;

import com.househost.privacy.policy.domain.model.PrivacyPolicyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "privacy_policies")
public class PrivacyPolicyJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    Integer version;

    @Column(nullable = false, length = 180)
    String title;

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    String content;

    @Column(length = 71)
    String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    PrivacyPolicyStatus status;

    @Column(nullable = false)
    LocalDateTime effectiveAt;

    LocalDateTime publishedAt;

    Long publishedByUserId;

    @Column(name = "current_slot", unique = true, length = 20)
    String currentSlot;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    protected PrivacyPolicyJpaEntity() {
    }
}
