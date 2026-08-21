package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.domain.model.Cashier;
import com.househost.finance.cashier.domain.model.CashierStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(CashierPersistenceAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CashierPessimisticLockIntegrationTest {

    private final CashierPersistenceAdapter cashierPersistenceAdapter;
    private final PlatformTransactionManager platformTransactionManager;

    @Autowired
    CashierPessimisticLockIntegrationTest(
            CashierPersistenceAdapter cashierPersistenceAdapter,
            PlatformTransactionManager platformTransactionManager
    ) {
        this.cashierPersistenceAdapter = cashierPersistenceAdapter;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Test
    void serializesConcurrentCashierMutationsWithAPessimisticWriteLock() throws Exception {
        Long cashierId = createCashier();
        CountDownLatch firstLockAcquiredCountDownLatch = new CountDownLatch(1);
        CountDownLatch secondLockAttemptStartedCountDownLatch = new CountDownLatch(1);
        CountDownLatch releaseFirstLockCountDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstLockFuture = executorService.submit(() -> inTransaction(() -> {
                assertNotNull(cashierPersistenceAdapter.findByIdForUpdate(cashierId).orElse(null));
                firstLockAcquiredCountDownLatch.countDown();
                await(releaseFirstLockCountDownLatch);
            }));
            assertTrue(firstLockAcquiredCountDownLatch.await(5, TimeUnit.SECONDS));

            Future<?> secondLockFuture = executorService.submit(() -> inTransaction(() -> {
                secondLockAttemptStartedCountDownLatch.countDown();
                assertNotNull(cashierPersistenceAdapter.findByIdForUpdate(cashierId).orElse(null));
            }));

            assertTrue(secondLockAttemptStartedCountDownLatch.await(5, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> secondLockFuture.get(200, TimeUnit.MILLISECONDS));
            releaseFirstLockCountDownLatch.countDown();
            firstLockFuture.get(5, TimeUnit.SECONDS);
            secondLockFuture.get(5, TimeUnit.SECONDS);
        } finally {
            releaseFirstLockCountDownLatch.countDown();
            executorService.shutdownNow();
        }
    }

    private Long createCashier() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        return transactionTemplate.execute(status -> cashierPersistenceAdapter.save(
                new Cashier(
                        "Caixa concorrente",
                        null,
                        new BigDecimal("1000.00"),
                        CashierStatus.OPEN
                )
        ).getId());
    }

    private void inTransaction(Runnable operation) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.executeWithoutResult(status -> operation.run());
    }

    private void await(CountDownLatch countDownLatch) {
        try {
            if (!countDownLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Tempo excedido ao aguardar liberacao do lock.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Espera pelo lock foi interrompida.", exception);
        }
    }
}
