package com.househost.finance.service;

import com.househost.booking.model.Booking;
import com.househost.booking.repository.BookingRepository;
import com.househost.finance.dto.FinancialTransactionRequestDTO;
import com.househost.finance.dto.FinancialTransactionResponseDTO;
import com.househost.finance.model.FinancialTransaction;
import com.househost.finance.model.FinancialTransactionMethod;
import com.househost.finance.model.FinancialPartyType;
import com.househost.finance.model.FinancialTransactionStatus;
import com.househost.finance.model.FinancialTransactionSourceType;
import com.househost.finance.model.FinancialTransactionType;
import com.househost.finance.model.InstallmentPlanStatus;
import com.househost.finance.model.InstallmentPlanTransaction;
import com.househost.finance.model.InstallmentTransactionStatus;
import com.househost.finance.repository.FinancialTransactionRepository;
import com.househost.guest.model.Guest;
import com.househost.guest.service.GuestService;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinancialTransactionService {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final BookingRepository bookingRepository;
    private final CashierService cashierService;
    private final GuestService guestService;

    public FinancialTransactionService(FinancialTransactionRepository financialTransactionRepository, BookingRepository bookingRepository, CashierService cashierService, GuestService guestService) {
        this.financialTransactionRepository = financialTransactionRepository;
        this.bookingRepository = bookingRepository;
        this.cashierService = cashierService;
        this.guestService = guestService;
    }

    public FinancialTransactionResponseDTO create(FinancialTransactionRequestDTO request) {
        validateRequest(request);

        FinancialPartyType senderType = parsePartyType(request.senderType, "Tipo do pagante e obrigatorio.");
        FinancialPartyType receiverType = parsePartyType(request.receiverType, "Tipo do recebedor e obrigatorio.");
        validateParty(senderType, request.senderId, "Pagante");
        validateParty(receiverType, request.receiverId, "Recebedor");
        validateDifferentParties(senderType, request.senderId, receiverType, request.receiverId);
        Guest guest = findGuestIfPresent(request.guestId);
        if (guest == null) {
            guest = findGuestPartyIfPresent(senderType, request.senderId);
        }
        if (guest == null) {
            guest = findGuestPartyIfPresent(receiverType, request.receiverId);
        }
        FinancialTransactionType type = parseType(request.type);
        FinancialTransactionStatus status = parseStatus(request.status);
        FinancialTransactionMethod method = parseMethod(request.method);
        FinancialTransactionSourceType sourceType = parseSourceType(request.sourceType);
        validateSource(sourceType, request.sourceId);
        LocalDate transactionDate = normalizeDate(request.transactionDate);
        String description = normalizeRequired(request.description);
        FinancialTransaction transaction = createTransaction(senderType, request.senderId, receiverType, request.receiverId, guest, type, request.amount, transactionDate, description, method, status);
        transaction.setSource(sourceType, request.sourceId);

        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        guestService.refreshFinancialStatus(savedTransaction.getGuest());
        return new FinancialTransactionResponseDTO(savedTransaction);
    }

    public List<FinancialTransactionResponseDTO> findAll() {
        return financialTransactionRepository.findAll()
                .stream()
                .map(FinancialTransactionResponseDTO::new)
                .toList();
    }

    public FinancialTransactionResponseDTO findById(Long id) {
        FinancialTransaction transaction = findTransactionById(id);
        return new FinancialTransactionResponseDTO(transaction);
    }

    public FinancialTransactionResponseDTO update(Long id, FinancialTransactionRequestDTO request) {
        validateRequest(request);

        FinancialTransaction transaction = findTransactionById(id);
        if (transaction.getStatus() == FinancialTransactionStatus.SETTLED) {
            throw new FinanceException("Transacao financeira liquidada nao pode ser alterada.");
        }
        FinancialPartyType senderType = parsePartyType(request.senderType, "Tipo do pagante e obrigatorio.");
        FinancialPartyType receiverType = parsePartyType(request.receiverType, "Tipo do recebedor e obrigatorio.");
        validateParty(senderType, request.senderId, "Pagante");
        validateParty(receiverType, request.receiverId, "Recebedor");
        validateDifferentParties(senderType, request.senderId, receiverType, request.receiverId);
        Guest guest = findGuestIfPresent(request.guestId);
        if (guest == null) {
            guest = findGuestPartyIfPresent(senderType, request.senderId);
        }
        if (guest == null) {
            guest = findGuestPartyIfPresent(receiverType, request.receiverId);
        }
        FinancialTransactionType type = parseType(request.type);
        FinancialTransactionStatus status = parseStatus(request.status);
        FinancialTransactionMethod method = parseMethod(request.method);
        FinancialTransactionSourceType sourceType = parseSourceType(request.sourceType);
        validateSource(sourceType, request.sourceId);
        LocalDate transactionDate = normalizeDate(request.transactionDate);
        String description = normalizeRequired(request.description);
        transaction.updateTransaction(
                senderType,
                request.senderId,
                receiverType,
                request.receiverId,
                guest,
                type,
                request.amount,
                transactionDate,
                description,
                method
        );
        transaction.setStatus(status);
        transaction.setSource(sourceType, request.sourceId);

        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        guestService.refreshFinancialStatus(savedTransaction.getGuest());
        return new FinancialTransactionResponseDTO(savedTransaction);
    }

    @Transactional
    public FinancialTransactionResponseDTO toSettle(Long id) {
        FinancialTransaction transaction = findTransactionById(id);

        if (transaction.getStatus() == FinancialTransactionStatus.SETTLED) {
            throw new FinanceException("Transacao financeira ja esta liquidada.");
        }

        validateParty(transaction.getSenderType(), transaction.getSenderId(), "Pagante");
        validateParty(transaction.getReceiverType(), transaction.getReceiverId(), "Recebedor");
        validateDifferentParties(transaction.getSenderType(), transaction.getSenderId(), transaction.getReceiverType(), transaction.getReceiverId());

        BigDecimal amount = transaction.getAmount();
        validatePositiveAmount(amount);

        transaction.setStatus(FinancialTransactionStatus.SETTLED);
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            installmentPlanTransaction.setInstallmentPlanStatus(InstallmentPlanStatus.PAID);
            installmentPlanTransaction.getInstallments().forEach(installmentTransaction -> {
                installmentTransaction.setStatus(FinancialTransactionStatus.SETTLED);
                installmentTransaction.setInstallmentStatus(InstallmentTransactionStatus.SETTLED);
            });
        }
        cashierService.settleMovementsForTransaction(transaction.getId());
        settleSourceIfNeeded(transaction);
        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        guestService.refreshFinancialStatus(savedTransaction.getGuest());

        return new FinancialTransactionResponseDTO(savedTransaction);
    }

    public void delete(Long id) {
        FinancialTransaction transaction = findTransactionById(id);
        Guest guest = transaction.getGuest();
        cashierService.removeMovementsForTransaction(transaction.getId());
        if (guest != null) {
            guest.removeFinancialTransaction(transaction);
        }
        financialTransactionRepository.delete(transaction);
        guestService.refreshFinancialStatus(guest);
    }

    private FinancialTransaction findTransactionById(Long id) {
        if (id == null) {
            throw new FinanceException("Transacao financeira nao encontrada.");
        }

        return financialTransactionRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Transacao financeira nao encontrada."));
    }

    private void settleSourceIfNeeded(FinancialTransaction transaction) {
        if (transaction.getSourceType() != FinancialTransactionSourceType.BOOKING || transaction.getSourceId() == null) {
            return;
        }

        Booking booking = bookingRepository.findById(transaction.getSourceId())
                .orElseThrow(() -> new FinanceException("Reserva de origem da transacao financeira nao encontrada."));
        booking.registerSettledPayment(transaction.getAmount(), transaction.getTransactionDate());
        bookingRepository.save(booking);
    }

    private Guest findGuestIfPresent(Long guestId) {
        if (guestId == null) {
            return null;
        }

        return guestService.findGuestById(guestId);
    }

    private void validateRequest(FinancialTransactionRequestDTO request) {
        if (request == null) {
            throw new FinanceException("Dados da transacao financeira sao obrigatorios.");
        }

        if (isBlank(request.type)) {
            throw new FinanceException("Tipo da transacao financeira e obrigatorio.");
        }

        validatePositiveAmount(request.amount);

        if (request.senderId == null) {
            throw new FinanceException("Identificador do pagante da transacao financeira e obrigatorio.");
        }

        if (request.receiverId == null) {
            throw new FinanceException("Identificador do recebedor da transacao financeira e obrigatorio.");
        }

        if (isBlank(request.description)) {
            throw new FinanceException("Descricao da transacao financeira e obrigatoria.");
        }
    }

    private FinancialTransactionType parseType(String type) {
        try {
            return FinancialTransactionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new FinanceException("Tipo da transacao financeira invalido. Use ENTRY, EXPENSE ou TRANSFER.");
        }
    }

    private FinancialPartyType parsePartyType(String type, String requiredMessage) {
        if (isBlank(type)) {
            throw new FinanceException(requiredMessage);
        }

        try {
            return FinancialPartyType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new FinanceException("Tipo de participante financeiro invalido. Use CASHIER ou GUEST.");
        }
    }

    private FinancialTransactionStatus parseStatus(String status) {
        if (isBlank(status)) {
            return FinancialTransactionStatus.WAITING;
        }

        try {
            return FinancialTransactionStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new FinanceException("Status da transacao financeira invalido.");
        }
    }

    private FinancialTransactionSourceType parseSourceType(String sourceType) {
        if (isBlank(sourceType)) {
            return null;
        }

        try {
            return FinancialTransactionSourceType.valueOf(sourceType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new FinanceException("Tipo de origem da transacao invalido.");
        }
    }

    private void validateSource(FinancialTransactionSourceType sourceType, Long sourceId) {
        if (sourceType != null && sourceId == null) {
            throw new FinanceException("Identificador da origem da transacao e obrigatorio quando sourceType e informado.");
        }
    }

    private FinancialTransactionMethod parseMethod(String method) {
        if (isBlank(method)) {
            return null;
        }

        return switch (method.trim().toUpperCase().replace(" ", "_")) {
            case "PIX" -> FinancialTransactionMethod.PIX;
            case "CARTAO_DE_CREDITO", "CARTÃO_DE_CRÉDITO", "CREDIT_CARD" -> FinancialTransactionMethod.CREDIT_CARD;
            case "CARTAO_DE_DEBITO", "CARTÃO_DE_DÉBITO", "DEBIT_CARD" -> FinancialTransactionMethod.DEBIT_CARD;
            case "DINHEIRO", "CASH" -> FinancialTransactionMethod.CASH;
            case "TRANSFERENCIA_BANCARIA", "TRANSFERÊNCIA_BANCÁRIA", "BANK_TRANSFER" -> FinancialTransactionMethod.BANK_TRANSFER;
            case "BOOKING" -> FinancialTransactionMethod.BOOKING;
            case "AIRBNB" -> FinancialTransactionMethod.AIRBNB;
            default -> throw new FinanceException("Metodo da transacao financeira invalido.");
        };
    }

    private FinancialTransaction createTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, FinancialTransactionStatus status) {
        return new FinancialTransaction(senderType, senderId, receiverType, receiverId, guest, type, amount, transactionDate, description, method, status);
    }

    private void validateParty(FinancialPartyType partyType, Long partyId, String role) {
        if (partyType == FinancialPartyType.CASHIER) {
            cashierService.findCashierById(partyId);
            return;
        }

        if (partyType == FinancialPartyType.GUEST) {
            guestService.findGuestById(partyId);
            return;
        }

        throw new FinanceException(role + " invalido.");
    }

    private Guest findGuestPartyIfPresent(FinancialPartyType partyType, Long partyId) {
        if (partyType == FinancialPartyType.GUEST) {
            return guestService.findGuestById(partyId);
        }

        return null;
    }

    private void validateDifferentParties(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId) {
        if (senderType == receiverType && senderId.equals(receiverId)) {
            throw new FinanceException("Pagante e recebedor devem ser diferentes.");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException("Valor da transacao financeira deve ser maior que zero.");
        }
    }

    private LocalDate normalizeDate(LocalDate date) {
        if (date == null) {
            return LocalDate.now();
        }

        return date;
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
