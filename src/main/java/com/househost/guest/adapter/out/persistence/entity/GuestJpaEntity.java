package com.househost.guest.adapter.out.persistence.entity;

import com.househost.guest.domain.model.GuestFinancialStatus;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.guest.domain.model.GuestType;
import com.househost.guest.domain.model.Guest;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guests")
public class GuestJpaEntity extends Guest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false) String fullName;
    @Column(unique = true) String email;
    String phone;
    @Column(unique = true) String documentNumber;
    String city;
    String state;
    String address;
    LocalDate birthDate;
    String gender;
    @Enumerated(EnumType.STRING) @Column(nullable = false) GuestType guestType = GuestType.REGULAR;
    @Enumerated(EnumType.STRING) @Column(nullable = false) GuestStatus status = GuestStatus.IN_BOOKING;
    @Enumerated(EnumType.STRING) @Column(nullable = false) GuestFinancialStatus financialStatus = GuestFinancialStatus.PAYMENT_SETTLED;
    boolean travelsWithPets;
    String petType;
    boolean needsAccessibility;
    String favoriteRoom;
    Integer stayCount;
    BigDecimal totalSpent;
    LocalDate lastStayDate;
    Integer rating;
    String originChannel;
    String referredBy;
    @Column(columnDefinition = "TEXT") String notes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guest_preferences", joinColumns = @JoinColumn(name = "guest_id"))
    @Column(name = "preference")
    List<String> preferences = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guest_financial_transaction_ids", joinColumns = @JoinColumn(name = "guest_id"))
    @Column(name = "financial_transaction_id", nullable = false)
    List<Long> financialTransactionIds = new ArrayList<>();

    @Column(nullable = false, updatable = false) LocalDateTime createdAt;
    @Column(nullable = false) LocalDateTime updatedAt;

    protected GuestJpaEntity() {
    }

    public static GuestJpaEntity reference(Long id) {
        GuestJpaEntity entity = new GuestJpaEntity();
        entity.id = id;
        return entity;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
}
