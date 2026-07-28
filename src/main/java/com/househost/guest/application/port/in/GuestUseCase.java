package com.househost.guest.application.port.in;

import com.househost.guest.application.dto.GuestContactResponseDTO;
import com.househost.guest.application.dto.GuestLookupResponseDTO;
import com.househost.guest.application.dto.GuestRegisterRequestDTO;
import com.househost.guest.application.dto.GuestRegisterResponseDTO;

import java.util.List;

public interface GuestUseCase {

    GuestRegisterResponseDTO guestRegister(GuestRegisterRequestDTO request);

    List<GuestRegisterResponseDTO> findAll(boolean masked);

    GuestRegisterResponseDTO findById(Long id, boolean masked);

    List<GuestLookupResponseDTO> findGuestsByName(String name);

    List<GuestLookupResponseDTO> findGuestsByDocumentNumber(String documentNumber);

    List<GuestLookupResponseDTO> findGuestsByEmail(String email);

    List<GuestLookupResponseDTO> findGuestsByPhone(String phone);

    List<GuestLookupResponseDTO> findGuestsByCity(String city);

    GuestContactResponseDTO revealContact(Long id);

    GuestRegisterResponseDTO findByIdForEdit(Long id);

    GuestRegisterResponseDTO update(Long id, GuestRegisterRequestDTO request);

    GuestRegisterResponseDTO delete(Long id);
}
