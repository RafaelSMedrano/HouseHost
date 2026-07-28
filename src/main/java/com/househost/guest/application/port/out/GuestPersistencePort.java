package com.househost.guest.application.port.out;

import com.househost.guest.domain.model.Guest;

import java.util.List;
import java.util.Optional;

public interface GuestPersistencePort {
    Guest save(Guest guest);
    List<Guest> findAll();
    Optional<Guest> findById(Long id);
    Optional<Guest> findByEmail(String email);
    Optional<Guest> findByDocumentNumber(String documentNumber);
    List<Guest> findByFullNameIgnoreCase(String fullName);
    List<Guest> findByFullNameContaining(String fullName);
    List<Guest> findByDocumentNumberContaining(String documentNumber);
    List<Guest> findByEmailContaining(String email);
    List<Guest> findByPhoneContaining(String phone);
    List<Guest> findByCityContaining(String city);
    void delete(Guest guest);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumberAndIdNot(String documentNumber, Long id);
}
