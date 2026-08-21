package com.househost.booking.checkout.application.service;

import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutResponseDTO;
import com.househost.booking.checkout.application.port.in.CheckOutUseCase;
import com.househost.booking.checkout.application.port.out.CheckOutAuditPort;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.application.service.BookingService;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CheckOutService implements CheckOutUseCase {

    private final CheckOutPersistencePort checkOutRepository;
    private final BookingService bookingService;
    private final CheckOutParticipantNotifier checkOutParticipantNotifier;
    private final CheckOutAuditPort checkOutAuditPort;
    private final CheckOutValidationService checkOutValidationService;

    public CheckOutService(
            CheckOutPersistencePort checkOutRepository,
            BookingService bookingService,
            CheckOutParticipantNotifier checkOutParticipantNotifier,
            CheckOutAuditPort checkOutAuditPort,
            CheckOutValidationService checkOutValidationService
    ) {
        this.checkOutRepository = checkOutRepository;
        this.bookingService = bookingService;
        this.checkOutParticipantNotifier = checkOutParticipantNotifier;
        this.checkOutAuditPort = checkOutAuditPort;
        this.checkOutValidationService = checkOutValidationService;
    }

    @Override
    @Transactional
    public CheckOutResponseDTO create(CheckOutRequestDTO request) {
        checkOutValidationService.validateRequest(request);
        Booking booking = bookingService.findBooking(request.bookingId);
        checkOutValidationService.validateUnique(booking.getId(), null);
        CheckOutStatus status = request.status == null ? CheckOutStatus.COMPLETED : request.status;
        CheckOut checkOut = buildCheckOut(booking, request, status);

        return saveWithCompletionEffects(
                checkOut,
                request,
                "CHECK_OUT_CREATED"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckOutResponseDTO> findAll() {
        List<CheckOutResponseDTO> checkOutResponseDTOList = checkOutRepository.findAll().stream()
                .map(CheckOutResponseDTO::new)
                .toList();
        checkOutAuditPort.record(
                "CHECK_OUT_LIST_VIEWED",
                null,
                Map.of("resultCount", checkOutResponseDTOList.size())
        );
        return checkOutResponseDTOList;
    }

    public List<CheckOut> findAllCheckOuts() {
        return checkOutRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CheckOut findCheckOutByBookingId(Long bookingId) {
        if (bookingId == null) {
            return null;
        }
        return checkOutRepository.findByBookingId(bookingId).orElse(null);
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
        checkOutValidationService.validateRequest(request);
        CheckOut checkOut = findCheckOutByIdForUpdate(id);
        Booking booking = bookingService.findBooking(request.bookingId);
        checkOutValidationService.validateUnique(booking.getId(), id);
        CheckOutStatus status = request.status == null ? CheckOutStatus.COMPLETED : request.status;

        checkOut.updateCheckOut(
                booking,
                booking.getGuest(),
                booking.getRoom(),
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

        return saveWithCompletionEffects(
                checkOut,
                request,
                "CHECK_OUT_UPDATED"
        );
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
                booking,
                booking.getGuest(),
                booking.getRoom(),
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
    }

    private CheckOutResponseDTO saveWithCompletionEffects(
            CheckOut checkOut,
            CheckOutRequestDTO checkOutRequestDTO,
            String auditEventType
    ) {
        CheckOut savedCheckOut = checkOutRepository.save(checkOut);
        boolean shouldApplyCompletionEffects = savedCheckOut.shouldApplyGuestHistory();
        Optional<FinancialTransactionPlanReplacementOutcomeDTO>
                paymentMaterializationOutcomeDTOOptional =
                checkOutParticipantNotifier.notifyCompletion(
                        savedCheckOut,
                        checkOutRequestDTO.rating,
                        checkOutRequestDTO.paymentMaterialization
                );
        if (shouldApplyCompletionEffects) {
            savedCheckOut.markGuestHistoryApplied();
            savedCheckOut = checkOutRepository.save(savedCheckOut);
        }
        checkOutAuditPort.record(
                auditEventType,
                savedCheckOut.getId(),
                Map.of("status", savedCheckOut.getStatus().name())
        );
        return new CheckOutResponseDTO(
                savedCheckOut,
                paymentMaterializationOutcomeDTOOptional.orElse(null)
        );
    }

    private CheckOut findCheckOutById(Long id) {
        if (id == null) {
            throw new BookingException("Check-out nao encontrado.");
        }
        return checkOutRepository.findById(id)
                .orElseThrow(() -> new BookingException("Check-out nao encontrado."));
    }

    private CheckOut findCheckOutByIdForUpdate(Long id) {
        if (id == null) {
            throw new BookingException("Check-out nao encontrado.");
        }
        return checkOutRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BookingException("Check-out nao encontrado."));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
