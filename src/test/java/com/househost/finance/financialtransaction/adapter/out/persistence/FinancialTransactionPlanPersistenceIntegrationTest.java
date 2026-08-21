package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(FinancialTransactionPlanPersistenceAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class FinancialTransactionPlanPersistenceIntegrationTest {

    private final FinancialTransactionPlanPersistenceAdapter
            financialTransactionPlanPersistenceAdapter;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final FinancialTransactionJpaRepository financialTransactionJpaRepository;

    @Autowired
    FinancialTransactionPlanPersistenceIntegrationTest(
            FinancialTransactionPlanPersistenceAdapter
                    financialTransactionPlanPersistenceAdapter,
            PlatformTransactionManager platformTransactionManager,
            EntityManagerFactory entityManagerFactory,
            FinancialTransactionJpaRepository financialTransactionJpaRepository
    ) {
        this.financialTransactionPlanPersistenceAdapter =
                financialTransactionPlanPersistenceAdapter;
        this.platformTransactionManager = platformTransactionManager;
        this.entityManagerFactory = entityManagerFactory;
        this.financialTransactionJpaRepository = financialTransactionJpaRepository;
    }

    @Test
    void preservesPlanMembershipStableOrderVersionAndInstallmentOwnership() {
        FinancialTransaction firstFinancialTransaction = financialTransaction(
                new BigDecimal("100.00"),
                LocalDate.of(2027, 1, 15),
                FinancialTransactionType.PLAN_DOWN_PAYMENT
        );
        InstallmentPlanTransaction installmentPlanTransaction = installmentPlanTransaction();
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.save(plan(List.of(
                        firstFinancialTransaction,
                        installmentPlanTransaction
                )));

        FinancialTransactionPlan restoredFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.findById(
                        savedFinancialTransactionPlan.getId()
                ).orElseThrow();

        assertNotNull(restoredFinancialTransactionPlan.getId());
        assertNotNull(restoredFinancialTransactionPlan.getVersion());
        assertEquals(new BigDecimal("400.00"), restoredFinancialTransactionPlan.getTotalAmount());
        assertEquals(2, restoredFinancialTransactionPlan.getFinancialTransactionCount());
        assertEquals(
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                restoredFinancialTransactionPlan.getFinancialTransactionList().get(0).getType()
        );
        assertEquals(
                1,
                restoredFinancialTransactionPlan.getFinancialTransactionList()
                        .get(0)
                        .getPlanComponentOrder()
        );
        InstallmentPlanTransaction restoredInstallmentPlanTransaction = assertInstanceOf(
                InstallmentPlanTransaction.class,
                restoredFinancialTransactionPlan.getFinancialTransactionList().get(1)
        );
        assertEquals(2, restoredInstallmentPlanTransaction.getPlanComponentOrder());
        assertEquals(
                FinancialTransactionSourceType.PLAN,
                restoredInstallmentPlanTransaction.getSourceType()
        );
        assertTrue(restoredInstallmentPlanTransaction.getInstallments().stream().allMatch(
                installmentTransaction -> installmentTransaction.getSourceType()
                        == FinancialTransactionSourceType.INSTALLMENT
                        && restoredInstallmentPlanTransaction.getId().equals(
                                installmentTransaction.getSourceId()
                        )
        ));
    }

    @Test
    void loadsCompleteAggregateWithThreeBoundedStatements() {
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.save(plan(List.of(
                        financialTransaction(
                                new BigDecimal("100.00"),
                                LocalDate.of(2027, 1, 15),
                                FinancialTransactionType.PLAN_DOWN_PAYMENT
                        ),
                        installmentPlanTransaction()
                )));
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        FinancialTransactionPlan restoredFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.findById(
                        savedFinancialTransactionPlan.getId()
                ).orElseThrow();

        assertEquals(4, restoredFinancialTransactionPlan.getSettlementFinancialTransactionList().size());
        assertTrue(statistics.getPrepareStatementCount() <= 3);
    }

    @Test
    void rejectsAStaleAggregateVersion() {
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.save(plan(List.of(
                        financialTransaction(
                                new BigDecimal("100.00"),
                                LocalDate.of(2027, 1, 15),
                                FinancialTransactionType.PLAN_DOWN_PAYMENT
                        )
                )));
        FinancialTransactionPlan firstFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.findById(
                        savedFinancialTransactionPlan.getId()
                ).orElseThrow();
        FinancialTransactionPlan staleFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.findById(
                        savedFinancialTransactionPlan.getId()
                ).orElseThrow();
        addPlanTransaction(firstFinancialTransactionPlan, new BigDecimal("50.00"));
        addPlanTransaction(staleFinancialTransactionPlan, new BigDecimal("75.00"));

        financialTransactionPlanPersistenceAdapter.save(firstFinancialTransactionPlan);

        assertThrows(
                OptimisticLockingFailureException.class,
                () -> financialTransactionPlanPersistenceAdapter.save(
                        staleFinancialTransactionPlan
                )
        );
    }

    @Test
    void persistsRemovalAndPhysicallyDeletesAnEligibleAggregateWithoutOrphans() {
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.save(plan(List.of(
                        financialTransaction(
                                new BigDecimal("100.00"),
                                LocalDate.of(2027, 1, 15),
                                FinancialTransactionType.PLAN_DOWN_PAYMENT
                        ),
                        financialTransaction(
                                new BigDecimal("200.00"),
                                LocalDate.of(2027, 2, 15),
                                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT
                        )
                )));
        FinancialTransactionPlan financialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.findById(
                        savedFinancialTransactionPlan.getId()
                ).orElseThrow();
        Long removedFinancialTransactionId = financialTransactionPlan
                .getFinancialTransactionList()
                .get(0)
                .getId();
        financialTransactionPlan.removeFinancialTransaction(removedFinancialTransactionId);

        FinancialTransactionPlan updatedFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.save(financialTransactionPlan);
        FinancialTransactionPlan restoredFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.findById(
                        updatedFinancialTransactionPlan.getId()
                ).orElseThrow();

        assertEquals(1, restoredFinancialTransactionPlan.getFinancialTransactionCount());
        assertTrue(financialTransactionJpaRepository.findById(
                removedFinancialTransactionId
        ).isEmpty());

        financialTransactionPlanPersistenceAdapter.delete(restoredFinancialTransactionPlan);

        assertTrue(financialTransactionPlanPersistenceAdapter.findById(
                restoredFinancialTransactionPlan.getId()
        ).isEmpty());
        assertTrue(financialTransactionJpaRepository.findDirectPlanComponentList(
                FinancialTransactionSourceType.PLAN,
                restoredFinancialTransactionPlan.getId()
        ).isEmpty());
    }

    @Test
    void serializesConcurrentMutationLookupWithAPessimisticWriteLock() throws Exception {
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistenceAdapter.save(plan(List.of(
                        financialTransaction(
                                new BigDecimal("100.00"),
                                LocalDate.of(2027, 1, 15),
                                FinancialTransactionType.PLAN_DOWN_PAYMENT
                        )
                )));
        CountDownLatch firstLockAcquiredCountDownLatch = new CountDownLatch(1);
        CountDownLatch secondLockAttemptStartedCountDownLatch = new CountDownLatch(1);
        CountDownLatch releaseFirstLockCountDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstLockFuture = executorService.submit(() -> inTransaction(() -> {
                assertTrue(financialTransactionPlanPersistenceAdapter.findByIdForUpdate(
                        savedFinancialTransactionPlan.getId()
                ).isPresent());
                firstLockAcquiredCountDownLatch.countDown();
                await(releaseFirstLockCountDownLatch);
            }));
            assertTrue(firstLockAcquiredCountDownLatch.await(5, TimeUnit.SECONDS));

            Future<?> secondLockFuture = executorService.submit(() -> inTransaction(() -> {
                secondLockAttemptStartedCountDownLatch.countDown();
                assertTrue(financialTransactionPlanPersistenceAdapter.findByIdForUpdate(
                        savedFinancialTransactionPlan.getId()
                ).isPresent());
            }));
            assertTrue(secondLockAttemptStartedCountDownLatch.await(5, TimeUnit.SECONDS));
            assertThrows(
                    TimeoutException.class,
                    () -> secondLockFuture.get(200, TimeUnit.MILLISECONDS)
            );

            releaseFirstLockCountDownLatch.countDown();
            firstLockFuture.get(5, TimeUnit.SECONDS);
            secondLockFuture.get(5, TimeUnit.SECONDS);
        } finally {
            releaseFirstLockCountDownLatch.countDown();
            executorService.shutdownNow();
        }
    }

    private FinancialTransactionPlan plan(
            List<FinancialTransaction> financialTransactionList
    ) {
        return new FinancialTransactionPlan(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionSourceType.BOOKING,
                30L,
                financialTransactionList,
                LocalDate.of(2027, 12, 31),
                "Plano persistente da reserva"
        );
    }

    private FinancialTransaction financialTransaction(
            BigDecimal amount,
            LocalDate dueDate,
            FinancialTransactionType financialTransactionType
    ) {
        return new FinancialTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                financialTransactionType,
                amount,
                LocalDate.of(2027, 1, 15),
                dueDate,
                "Componente persistente",
                FinancialTransactionMethod.PIX,
                FinancialTransactionStatus.WAITING
        );
    }

    private InstallmentPlanTransaction installmentPlanTransaction() {
        return new InstallmentPlanTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                new BigDecimal("300.00"),
                LocalDate.of(2027, 1, 15),
                "Bloco persistente",
                FinancialTransactionMethod.CREDIT_CARD,
                3,
                15,
                FinancialTransactionType.INSTALLMENT_PLAN_BLOCK
        );
    }

    private void addPlanTransaction(
            FinancialTransactionPlan financialTransactionPlan,
            BigDecimal amount
    ) {
        FinancialTransaction financialTransaction = financialTransaction(
                amount,
                LocalDate.of(2027, 2, 15),
                FinancialTransactionType.PLAN_TRANSACTION
        );
        financialTransaction.assignPlanMembership(
                financialTransactionPlan.getId(),
                financialTransactionPlan.getFinancialTransactionCount() + 1
        );
        financialTransactionPlan.addFinancialTransaction(financialTransaction);
    }

    private void inTransaction(Runnable operation) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.executeWithoutResult(status -> operation.run());
    }

    private void await(CountDownLatch countDownLatch) {
        try {
            if (!countDownLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Tempo excedido ao aguardar lock do plano financeiro.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Espera pelo lock do plano foi interrompida.", exception);
        }
    }
}
