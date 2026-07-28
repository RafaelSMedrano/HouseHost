package com.househost.guest.application.service;

import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.guest.application.port.in.GuestFinancialTransactionUseCase;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestFinancialStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GuestFinancialService implements GuestFinancialTransactionUseCase {

    private final GuestService guestService;
    private final GuestPersistencePort guestRepository;
    private final FinancialTransactionPersistencePort financialTransactionRepository;

    public GuestFinancialService(
            GuestService guestService,
            GuestPersistencePort guestRepository,
            FinancialTransactionPersistencePort financialTransactionRepository
    ) {
        this.guestService = guestService;
        this.guestRepository = guestRepository;
        this.financialTransactionRepository = financialTransactionRepository;
    }

    @Override
    public void relateFinancialTransaction(Long guestId, Long financialTransactionId) {
        Guest guest = guestService.findGuestById(guestId);
        guest.addFinancialTransactionId(financialTransactionId);
        guestRepository.save(guest);
    }

    @Override
    public void removeFinancialTransactionRelation(Long guestId, Long financialTransactionId) {
        Guest guest = guestService.findGuestById(guestId);
        guest.removeFinancialTransactionId(financialTransactionId);
        guestRepository.save(guest);
    }

    @Override
    public void updateFinancialStatus(Long guestId) {
        Guest guest = guestService.findGuestById(guestId);
        List<FinancialTransaction> waitingTransactions = guest.getFinancialTransactionIds().stream()
                .map(financialTransactionRepository::findById)
                .flatMap(java.util.Optional::stream)
                .filter(transaction -> transaction.getSenderType() == FinancialPartyType.GUEST)
                .filter(transaction -> guestId.equals(transaction.getSenderId()))
                .filter(transaction -> transaction.getStatus() == FinancialTransactionStatus.WAITING)
                .toList();

        LocalDate today = LocalDate.now();
        GuestFinancialStatus financialStatus = waitingTransactions.stream()
                .anyMatch(transaction -> transaction.getDueDate().isBefore(today))
                ? GuestFinancialStatus.DEBTOR
                : waitingTransactions.isEmpty()
                        ? GuestFinancialStatus.PAYMENT_SETTLED
                        : GuestFinancialStatus.WAITING_PAYMENT;

        guest.changeFinancialStatus(financialStatus);
        guestRepository.save(guest);
    }
}
