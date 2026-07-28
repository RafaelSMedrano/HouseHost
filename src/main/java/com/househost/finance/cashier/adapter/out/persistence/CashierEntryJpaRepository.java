package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface CashierEntryJpaRepository extends JpaRepository<CashierEntryJpaEntity, Long> {

    List<CashierEntryJpaEntity> findByCashier_Id(Long cashierId);

    List<CashierEntryJpaEntity> findBySourceTransaction_Id(Long sourceTransactionId);

    Optional<CashierEntryJpaEntity> findBySourceTransaction_IdAndCashier_Id(
            Long sourceTransactionId,
            Long cashierId
    );

}
