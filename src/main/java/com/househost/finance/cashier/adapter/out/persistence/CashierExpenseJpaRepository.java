package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierExpenseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface CashierExpenseJpaRepository extends JpaRepository<CashierExpenseJpaEntity, Long> {

    List<CashierExpenseJpaEntity> findByCashier_Id(Long cashierId);

    List<CashierExpenseJpaEntity> findBySourceTransaction_Id(Long sourceTransactionId);

    Optional<CashierExpenseJpaEntity> findBySourceTransaction_IdAndCashier_Id(
            Long sourceTransactionId,
            Long cashierId
    );

}
