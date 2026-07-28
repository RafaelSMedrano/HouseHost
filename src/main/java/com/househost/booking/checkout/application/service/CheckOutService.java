package com.househost.booking.checkout.application.service;

import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutResponseDTO;
import com.househost.booking.checkout.application.port.in.CheckOutUseCase;
import com.househost.booking.checkout.application.port.out.CheckOutAuditPort;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CheckOutService implements CheckOutUseCase {
    private final CheckOutPersistencePort checkOutRepository;
    private final CheckOutPartyResolverService partyResolverService;
    private final CheckOutAuditPort checkOutAuditPort;
    private final CheckOutValidationService validationService;

    public CheckOutService(CheckOutPersistencePort checkOutRepository,
                           CheckOutPartyResolverService partyResolverService,
                           CheckOutAuditPort checkOutAuditPort,
                           CheckOutValidationService validationService) {
        this.checkOutRepository = checkOutRepository;
        this.partyResolverService = partyResolverService;
        this.checkOutAuditPort = checkOutAuditPort;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public CheckOutResponseDTO create(CheckOutRequestDTO request) {
        validationService.validateRequest(request);
        Booking booking = partyResolverService.findBooking(request.bookingId);
        validationService.validateUnique(booking.getId(), null);
        CheckOutStatus status = request.status == null ? CheckOutStatus.COMPLETED : request.status;
        CheckOut checkOut = buildCheckOut(booking, request, status);

        CheckOut savedCheckOut = checkOutRepository.save(checkOut);
        if (savedCheckOut.getStatus() == CheckOutStatus.COMPLETED) {
            partyResolverService.resolveParties(savedCheckOut);
        }
        checkOutAuditPort.record("CHECK_OUT_CREATED", savedCheckOut.getId(), Map.of("status", savedCheckOut.getStatus().name()));
        return new CheckOutResponseDTO(savedCheckOut);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckOutResponseDTO> findAll() {
        List<CheckOutResponseDTO> checkOuts = checkOutRepository.findAll().stream()
                .map(CheckOutResponseDTO::new)
                .toList();
        checkOutAuditPort.record("CHECK_OUT_LIST_VIEWED", null, Map.of("resultCount", checkOuts.size()));
        return checkOuts;
    }

    public List<CheckOut> findAllCheckOuts() {
        return checkOutRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckOutResponseDTO findById(Long id) {
        CheckOut checkOut = findCheckOutById(id);
        checkOutAuditPort.record("CHECK_OUT_VIEWED", checkOut.getId(), Map.of());
        return new CheckOutResponseDTO(checkOut);
    }

    @Override
    @Transactional
    public CheckOutResponseDTO update(Long id, CheckOutRequestDTO request) {
        validationService.validateRequest(request);
        CheckOut checkOut = findCheckOutById(id);
        Booking booking = partyResolverService.findBooking(request.bookingId);
        validationService.validateUnique(booking.getId(), id);
        CheckOutStatus status = request.status == null ? CheckOutStatus.COMPLETED : request.status;

        checkOut.updateCheckOut(
                booking, booking.getGuest(), booking.getRoom(), request.actualCheckOutAt,
                request.roomInspected, request.keysReturned, request.consumablesChecked,
                request.pendingAmountPaid, request.extraCharges, request.pendingAmount,
                normalizeOptional(request.performedBy), normalizeOptional(request.notes), status
        );

        CheckOut savedCheckOut = checkOutRepository.save(checkOut);
        if (savedCheckOut.getStatus() == CheckOutStatus.COMPLETED) {
            partyResolverService.resolveParties(savedCheckOut);
        }
        checkOutAuditPort.record("CHECK_OUT_UPDATED", savedCheckOut.getId(), Map.of("status", savedCheckOut.getStatus().name()));
        return new CheckOutResponseDTO(savedCheckOut);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CheckOut checkOut = findCheckOutById(id);
        checkOutRepository.delete(checkOut);
        checkOutAuditPort.record("CHECK_OUT_DELETED", checkOut.getId(), Map.of());
    }

    private CheckOut buildCheckOut(Booking booking, CheckOutRequestDTO request, CheckOutStatus status) {
        return new CheckOut(
                booking, booking.getGuest(), booking.getRoom(), request.actualCheckOutAt,
                request.roomInspected, request.keysReturned, request.consumablesChecked,
                request.pendingAmountPaid, request.extraCharges, request.pendingAmount,
                normalizeOptional(request.performedBy), normalizeOptional(request.notes), status
        );
    }

    private CheckOut findCheckOutById(Long id) {
        if (id == null) throw new BookingException("Check-out nao encontrado.");
        return checkOutRepository.findById(id)
                .orElseThrow(() -> new BookingException("Check-out nao encontrado."));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
