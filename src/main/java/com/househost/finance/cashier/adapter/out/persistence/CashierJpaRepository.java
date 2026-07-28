package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface CashierJpaRepository extends JpaRepository<CashierJpaEntity, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
