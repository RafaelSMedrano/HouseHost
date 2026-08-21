package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.port.out.FinancialParty;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionSource;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FinancialParticipantNotifierTest {

    @Test
    void notifiesParticipantsBeforeSourceOnCompleteSettlement() {
        FinancialPartyResolver financialPartyResolver = mock(FinancialPartyResolver.class);
        FinancialTransactionSourceResolver financialTransactionSourceResolver =
                mock(FinancialTransactionSourceResolver.class);
        FinancialParty senderFinancialParty = mock(FinancialParty.class);
        FinancialParty receiverFinancialParty = mock(FinancialParty.class);
        FinancialTransactionSource financialTransactionSource = mock(FinancialTransactionSource.class);
        FinancialTransaction transaction = transactionWithBookingSource();
        when(financialPartyResolver.resolve(FinancialPartyType.GUEST)).thenReturn(senderFinancialParty);
        when(financialPartyResolver.resolve(FinancialPartyType.CASHIER)).thenReturn(receiverFinancialParty);
        when(financialTransactionSourceResolver.resolve(FinancialTransactionSourceType.BOOKING))
                .thenReturn(financialTransactionSource);
        FinancialParticipantNotifier financialParticipantNotifier = new FinancialParticipantNotifier(
                financialPartyResolver,
                financialTransactionSourceResolver
        );

        financialParticipantNotifier.notifySettlement(transaction);

        var notificationOrder = inOrder(
                senderFinancialParty,
                receiverFinancialParty,
                financialTransactionSource
        );
        notificationOrder.verify(senderFinancialParty).onSettle(20L, transaction);
        notificationOrder.verify(receiverFinancialParty).onSettle(1L, transaction);
        notificationOrder.verify(financialTransactionSource).onSettle(30L, transaction);
    }

    @Test
    void doesNotNotifySourceOnIndividualInstallmentSettlement() {
        FinancialPartyResolver financialPartyResolver = mock(FinancialPartyResolver.class);
        FinancialTransactionSourceResolver financialTransactionSourceResolver =
                mock(FinancialTransactionSourceResolver.class);
        FinancialParty senderFinancialParty = mock(FinancialParty.class);
        FinancialParty receiverFinancialParty = mock(FinancialParty.class);
        FinancialTransaction installmentTransaction = transactionWithBookingSource();
        when(financialPartyResolver.resolve(FinancialPartyType.GUEST)).thenReturn(senderFinancialParty);
        when(financialPartyResolver.resolve(FinancialPartyType.CASHIER)).thenReturn(receiverFinancialParty);
        FinancialParticipantNotifier financialParticipantNotifier = new FinancialParticipantNotifier(
                financialPartyResolver,
                financialTransactionSourceResolver
        );

        financialParticipantNotifier.notifyInstallmentSettlement(installmentTransaction);

        verify(senderFinancialParty).onSettle(20L, installmentTransaction);
        verify(receiverFinancialParty).onSettle(1L, installmentTransaction);
        verifyNoInteractions(financialTransactionSourceResolver);
    }

    @Test
    void creationAndDeletionNotifyPartiesBeforeSource() {
        FinancialPartyResolver financialPartyResolver = mock(FinancialPartyResolver.class);
        FinancialTransactionSourceResolver financialTransactionSourceResolver =
                mock(FinancialTransactionSourceResolver.class);
        FinancialParty senderFinancialParty = mock(FinancialParty.class);
        FinancialParty receiverFinancialParty = mock(FinancialParty.class);
        FinancialTransactionSource financialTransactionSource = mock(FinancialTransactionSource.class);
        FinancialTransaction transaction = transactionWithBookingSource();
        when(financialPartyResolver.resolve(FinancialPartyType.GUEST)).thenReturn(senderFinancialParty);
        when(financialPartyResolver.resolve(FinancialPartyType.CASHIER)).thenReturn(receiverFinancialParty);
        when(financialTransactionSourceResolver.resolve(FinancialTransactionSourceType.BOOKING))
                .thenReturn(financialTransactionSource);
        FinancialParticipantNotifier financialParticipantNotifier = new FinancialParticipantNotifier(
                financialPartyResolver,
                financialTransactionSourceResolver
        );

        financialParticipantNotifier.notifyCreation(transaction);
        financialParticipantNotifier.notifyDeletion(transaction);

        var creationNotificationOrder = inOrder(
                senderFinancialParty,
                receiverFinancialParty,
                financialTransactionSource
        );
        creationNotificationOrder.verify(senderFinancialParty).onCreate(20L, transaction);
        creationNotificationOrder.verify(receiverFinancialParty).onCreate(1L, transaction);
        creationNotificationOrder.verify(financialTransactionSource).onCreate(30L, transaction);
        var deletionNotificationOrder = inOrder(
                senderFinancialParty,
                receiverFinancialParty,
                financialTransactionSource
        );
        deletionNotificationOrder.verify(senderFinancialParty).onDelete(transaction);
        deletionNotificationOrder.verify(receiverFinancialParty).onDelete(transaction);
        deletionNotificationOrder.verify(financialTransactionSource).onDelete(30L, transaction);
    }

    @Test
    void installmentBlockNotifiesIndividualInstallmentsAndPlanSourceOnce() {
        FinancialPartyResolver financialPartyResolver = mock(FinancialPartyResolver.class);
        FinancialTransactionSourceResolver financialTransactionSourceResolver =
                mock(FinancialTransactionSourceResolver.class);
        FinancialParty senderFinancialParty = mock(FinancialParty.class);
        FinancialParty receiverFinancialParty = mock(FinancialParty.class);
        FinancialTransactionSource financialTransactionSource = mock(FinancialTransactionSource.class);
        InstallmentPlanTransaction installmentPlanTransaction = new InstallmentPlanTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                new BigDecimal("250.00"),
                LocalDate.of(2026, 8, 13),
                "Sinal parcelado",
                FinancialTransactionMethod.PIX,
                2,
                15,
                FinancialTransactionType.PLAN_DOWN_PAYMENT
        );
        installmentPlanTransaction.setSource(FinancialTransactionSourceType.PLAN, 50L);
        when(financialPartyResolver.resolve(FinancialPartyType.GUEST)).thenReturn(senderFinancialParty);
        when(financialPartyResolver.resolve(FinancialPartyType.CASHIER)).thenReturn(receiverFinancialParty);
        when(financialTransactionSourceResolver.resolve(FinancialTransactionSourceType.PLAN))
                .thenReturn(financialTransactionSource);
        FinancialParticipantNotifier financialParticipantNotifier = new FinancialParticipantNotifier(
                financialPartyResolver,
                financialTransactionSourceResolver
        );

        financialParticipantNotifier.notifyCreation(installmentPlanTransaction);

        installmentPlanTransaction.getInstallments().forEach(installmentTransaction -> {
            verify(senderFinancialParty).onCreate(20L, installmentTransaction);
            verify(receiverFinancialParty).onCreate(1L, installmentTransaction);
        });
        verify(financialTransactionSource).onCreate(50L, installmentPlanTransaction);
    }

    @Test
    void settlementWithoutSourceOnlyNotifiesParticipants() {
        FinancialPartyResolver financialPartyResolver = mock(FinancialPartyResolver.class);
        FinancialTransactionSourceResolver financialTransactionSourceResolver =
                mock(FinancialTransactionSourceResolver.class);
        FinancialParty senderFinancialParty = mock(FinancialParty.class);
        FinancialParty receiverFinancialParty = mock(FinancialParty.class);
        FinancialTransaction transaction = transaction();
        when(financialPartyResolver.resolve(FinancialPartyType.GUEST)).thenReturn(senderFinancialParty);
        when(financialPartyResolver.resolve(FinancialPartyType.CASHIER)).thenReturn(receiverFinancialParty);
        FinancialParticipantNotifier financialParticipantNotifier = new FinancialParticipantNotifier(
                financialPartyResolver,
                financialTransactionSourceResolver
        );

        financialParticipantNotifier.notifySettlement(transaction);

        verify(senderFinancialParty).onSettle(20L, transaction);
        verify(receiverFinancialParty).onSettle(1L, transaction);
        verifyNoInteractions(financialTransactionSourceResolver);
    }

    @Test
    void deletionWithoutSourceOnlyNotifiesParticipants() {
        FinancialPartyResolver financialPartyResolver = mock(FinancialPartyResolver.class);
        FinancialTransactionSourceResolver financialTransactionSourceResolver =
                mock(FinancialTransactionSourceResolver.class);
        FinancialParty senderFinancialParty = mock(FinancialParty.class);
        FinancialParty receiverFinancialParty = mock(FinancialParty.class);
        FinancialTransaction transaction = transaction();
        when(financialPartyResolver.resolve(FinancialPartyType.GUEST)).thenReturn(senderFinancialParty);
        when(financialPartyResolver.resolve(FinancialPartyType.CASHIER)).thenReturn(receiverFinancialParty);
        FinancialParticipantNotifier financialParticipantNotifier = new FinancialParticipantNotifier(
                financialPartyResolver,
                financialTransactionSourceResolver
        );

        financialParticipantNotifier.notifyDeletion(transaction);

        verify(senderFinancialParty).onDelete(transaction);
        verify(receiverFinancialParty).onDelete(transaction);
        verifyNoInteractions(financialTransactionSourceResolver);
    }

    private FinancialTransaction transactionWithBookingSource() {
        FinancialTransaction transaction = transaction();
        transaction.setSource(FinancialTransactionSourceType.BOOKING, 30L);
        return transaction;
    }

    private FinancialTransaction transaction() {
        return new FinancialTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionType.STANDARD,
                new BigDecimal("250.00"),
                LocalDate.of(2026, 8, 13),
                "Pagamento de reserva"
        );
    }
}
