package com.househost.guest.adapter.out.persistence;

import com.househost.guest.adapter.out.persistence.entity.GuestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface GuestJpaRepository extends JpaRepository<GuestJpaEntity, Long> {
    Optional<GuestJpaEntity> findByEmail(String email);
    Optional<GuestJpaEntity> findByDocumentNumber(String documentNumber);
    List<GuestJpaEntity> findByFullNameIgnoreCase(String fullName);
    List<GuestJpaEntity> findTop8ByFullNameContainingIgnoreCase(String fullName);
    List<GuestJpaEntity> findTop8ByDocumentNumberContainingIgnoreCase(String documentNumber);
    List<GuestJpaEntity> findTop20ByEmailContainingIgnoreCase(String email);
    List<GuestJpaEntity> findTop20ByPhoneContainingIgnoreCase(String phone);
    List<GuestJpaEntity> findTop20ByCityContainingIgnoreCase(String city);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumberAndIdNot(String documentNumber, Long id);
}
