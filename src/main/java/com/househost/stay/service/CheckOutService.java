package com.househost.stay.service;

import com.househost.guest.model.Guest;
import com.househost.guest.model.GuestStatus;
import com.househost.guest.repository.GuestRepository;
import com.househost.shared.dto.ResponseDTO;
import com.househost.shared.exception.StayException;
import com.househost.stay.dto.CheckOutRequestDTO;
import com.househost.stay.dto.CheckOutResponseDTO;
import com.househost.stay.model.CheckOut;
import com.househost.stay.model.CheckOutStatus;
import com.househost.stay.model.Stay;
import com.househost.stay.model.StayStatus;
import com.househost.stay.repository.CheckOutRepository;
import com.househost.stay.repository.StayRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckOutService {

    private final CheckOutRepository checkOutRepository;
    private final StayRepository stayRepository;
    private final GuestRepository guestRepository;

    public CheckOutService(CheckOutRepository checkOutRepository, StayRepository stayRepository, GuestRepository guestRepository) {
        this.checkOutRepository = checkOutRepository;
        this.stayRepository = stayRepository;
        this.guestRepository = guestRepository;
    }

    public ResponseDTO create(CheckOutRequestDTO request) {
        validateRequest(request);
        Stay stay = findStayById(request.stayId);
        validateUnique(stay.getId());
        CheckOutStatus status = parseStatus(request.status);

        CheckOut checkOut = new CheckOut(
                stay,
                stay.getGuest(),
                stay.getRoom(),
                request.actualCheckOutAt,
                request.roomInspected,
                request.keysReturned,
                request.consumablesChecked,
                request.pendingAmountPaid,
                request.extraCharges,
                request.pendingAmount,
                normalizeOptional(request.performedBy),
                normalizeOptional(request.notes),
                status
        );

        if (status == CheckOutStatus.COMPLETED) {
            markStayCheckedOut(stay, request.actualCheckOutAt);
            markGuestCheckedOut(stay.getGuest());
        }

        CheckOut savedCheckOut = checkOutRepository.save(checkOut);
        return new ResponseDTO("success", "Check-out cadastrado com sucesso", new CheckOutResponseDTO(savedCheckOut));
    }

    public ResponseDTO findAll() {
        List<CheckOutResponseDTO> checkOuts = checkOutRepository.findAll()
                .stream()
                .map(CheckOutResponseDTO::new)
                .toList();
        return new ResponseDTO("success", "Check-outs encontrados com sucesso", checkOuts);
    }

    public ResponseDTO findById(Long id) {
        return new ResponseDTO("success", "Check-out encontrado com sucesso", new CheckOutResponseDTO(findCheckOutById(id)));
    }

    public ResponseDTO update(Long id, CheckOutRequestDTO request) {
        validateRequest(request);
        CheckOut checkOut = findCheckOutById(id);
        Stay stay = findStayById(request.stayId);
        validateUnique(stay.getId(), id);
        CheckOutStatus status = parseStatus(request.status);

        checkOut.updateCheckOut(
                stay,
                stay.getGuest(),
                stay.getRoom(),
                request.actualCheckOutAt,
                request.roomInspected,
                request.keysReturned,
                request.consumablesChecked,
                request.pendingAmountPaid,
                request.extraCharges,
                request.pendingAmount,
                normalizeOptional(request.performedBy),
                normalizeOptional(request.notes),
                status
        );

        if (status == CheckOutStatus.COMPLETED) {
            markStayCheckedOut(stay, request.actualCheckOutAt);
            markGuestCheckedOut(stay.getGuest());
        }

        CheckOut savedCheckOut = checkOutRepository.save(checkOut);
        return new ResponseDTO("success", "Check-out atualizado com sucesso", new CheckOutResponseDTO(savedCheckOut));
    }

    public ResponseDTO delete(Long id) {
        CheckOut checkOut = findCheckOutById(id);
        checkOutRepository.delete(checkOut);
        return new ResponseDTO("success", "Check-out removido com sucesso", null);
    }

    private CheckOut findCheckOutById(Long id) {
        if (id == null) {
            throw new StayException("Check-out nao encontrado.");
        }
        return checkOutRepository.findById(id)
                .orElseThrow(() -> new StayException("Check-out nao encontrado."));
    }

    private Stay findStayById(Long id) {
        if (id == null) {
            throw new StayException("Estadia e obrigatoria.");
        }
        return stayRepository.findById(id)
                .orElseThrow(() -> new StayException("Estadia nao encontrada."));
    }

    private void markStayCheckedOut(Stay stay, LocalDateTime actualCheckOutAt) {
        stay.updateStay(
                stay.getBooking(),
                stay.getGuest(),
                stay.getRoom(),
                stay.getCheckInDate(),
                stay.getExpectedCheckOutDate(),
                actualCheckOutAt == null ? LocalDate.now() : actualCheckOutAt.toLocalDate(),
                StayStatus.CHECKED_OUT,
                stay.getTotalAmount(),
                stay.getVehiclePlate(),
                stay.getVehicleModel()
        );
        stayRepository.save(stay);
    }

    private void markGuestCheckedOut(Guest guest) {
        guest.changeStatus(GuestStatus.GOT_CHECKOUT);
        guestRepository.save(guest);
    }

    private void validateRequest(CheckOutRequestDTO request) {
        if (request == null) {
            throw new StayException("Dados do check-out sao obrigatorios.");
        }
        if (request.stayId == null) {
            throw new StayException("Estadia e obrigatoria.");
        }
    }

    private void validateUnique(Long stayId) {
        validateUnique(stayId, null);
    }

    private void validateUnique(Long stayId, Long id) {
        checkOutRepository.findByStayId(stayId).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new StayException("Estadia ja possui check-out.");
            }
        });
    }

    private CheckOutStatus parseStatus(String status) {
        if (isBlank(status)) {
            return CheckOutStatus.COMPLETED;
        }
        try {
            return CheckOutStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new StayException("Status do check-out invalido. Use PENDING, COMPLETED ou CANCELLED.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
