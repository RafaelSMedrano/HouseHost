package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionPlanJpaEntity;
import com.househost.finance.financialtransaction.adapter.out.persistence.entity.InstallmentPlanTransactionJpaEntity;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPlanPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class FinancialTransactionPlanPersistenceAdapter
        implements FinancialTransactionPlanPersistencePort {

    private final FinancialTransactionPlanJpaRepository financialTransactionPlanJpaRepository;
    private final FinancialTransactionJpaRepository financialTransactionJpaRepository;
    private final InstallmentPlanTransactionJpaRepository installmentPlanTransactionJpaRepository;

    public FinancialTransactionPlanPersistenceAdapter(
            FinancialTransactionPlanJpaRepository financialTransactionPlanJpaRepository,
            FinancialTransactionJpaRepository financialTransactionJpaRepository,
            InstallmentPlanTransactionJpaRepository installmentPlanTransactionJpaRepository
    ) {
        this.financialTransactionPlanJpaRepository = financialTransactionPlanJpaRepository;
        this.financialTransactionJpaRepository = financialTransactionJpaRepository;
        this.installmentPlanTransactionJpaRepository = installmentPlanTransactionJpaRepository;
    }

    @Override
    @Transactional
    public FinancialTransactionPlan save(FinancialTransactionPlan financialTransactionPlan) {
        financialTransactionPlan.refreshDerivedState(LocalDate.now());
        FinancialTransactionPlanJpaEntity financialTransactionPlanJpaEntity =
                FinancialTransactionPlanPersistenceMapper.toEntity(financialTransactionPlan);
        FinancialTransactionPlanJpaEntity savedFinancialTransactionPlanJpaEntity =
                financialTransactionPlanJpaRepository.saveAndFlush(
                        financialTransactionPlanJpaEntity
                );
        if (financialTransactionPlan.getId() == null) {
            financialTransactionPlan.assignIdentity(savedFinancialTransactionPlanJpaEntity.getId());
        }
        deleteRemovedFinancialTransactionList(financialTransactionPlan);

        List<FinancialTransactionJpaEntity> financialTransactionJpaEntityList =
                financialTransactionPlan.getFinancialTransactionList().stream()
                        .map(FinancialTransactionPersistenceMapper::toEntity)
                        .toList();
        List<FinancialTransactionJpaEntity> savedFinancialTransactionJpaEntityList =
                financialTransactionJpaRepository.saveAllAndFlush(
                        financialTransactionJpaEntityList
                );
        List<FinancialTransaction> savedFinancialTransactionList =
                savedFinancialTransactionJpaEntityList.stream()
                        .map(FinancialTransactionPersistenceMapper::toDomain)
                        .toList();
        return FinancialTransactionPlanPersistenceMapper.toDomain(
                savedFinancialTransactionPlanJpaEntity,
                savedFinancialTransactionList
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinancialTransactionPlan> findById(Long id) {
        return findCompletePlan(() -> financialTransactionPlanJpaRepository.findById(id));
    }

    @Override
    @Transactional
    public Optional<FinancialTransactionPlan> findByIdForUpdate(Long id) {
        return findCompletePlan(
                () -> financialTransactionPlanJpaRepository.findByIdForUpdate(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinancialTransactionPlan> findBySource(
            FinancialTransactionSourceType sourceType,
            Long sourceId
    ) {
        return findCompletePlan(() -> financialTransactionPlanJpaRepository
                .findBySourceTypeAndSourceId(sourceType, sourceId));
    }

    @Override
    @Transactional
    public Optional<FinancialTransactionPlan> findBySourceForUpdate(
            FinancialTransactionSourceType sourceType,
            Long sourceId
    ) {
        return findCompletePlan(() -> financialTransactionPlanJpaRepository
                .findBySourceForUpdate(sourceType, sourceId));
    }

    @Override
    @Transactional
    public void delete(FinancialTransactionPlan financialTransactionPlan) {
        if (financialTransactionPlan == null || financialTransactionPlan.getId() == null) {
            throw new IllegalArgumentException("Plano financeiro persistido e obrigatorio.");
        }
        if (!financialTransactionPlan.isEligibleForPhysicalDeletion()) {
            throw new IllegalStateException("Plano com historico liquidado deve ser retido.");
        }
        List<FinancialTransactionJpaEntity> financialTransactionJpaEntityList =
                financialTransactionJpaRepository.findDirectPlanComponentList(
                        FinancialTransactionSourceType.PLAN,
                        financialTransactionPlan.getId()
                );
        financialTransactionJpaRepository.deleteAll(financialTransactionJpaEntityList);
        financialTransactionJpaRepository.flush();
        financialTransactionPlanJpaRepository.deleteById(financialTransactionPlan.getId());
    }

    private void deleteRemovedFinancialTransactionList(
            FinancialTransactionPlan financialTransactionPlan
    ) {
        List<Long> retainedFinancialTransactionIdList =
                financialTransactionPlan.getFinancialTransactionList().stream()
                        .map(FinancialTransaction::getId)
                        .filter(java.util.Objects::nonNull)
                        .toList();
        List<FinancialTransactionJpaEntity> removedFinancialTransactionJpaEntityList =
                financialTransactionJpaRepository.findDirectPlanComponentList(
                                FinancialTransactionSourceType.PLAN,
                                financialTransactionPlan.getId()
                        ).stream()
                        .filter(financialTransactionJpaEntity ->
                                !retainedFinancialTransactionIdList.contains(
                                        financialTransactionJpaEntity.getId()
                                )
                        )
                        .toList();
        financialTransactionJpaRepository.deleteAll(
                removedFinancialTransactionJpaEntityList
        );
        financialTransactionJpaRepository.flush();
    }

    private Optional<FinancialTransactionPlan> findCompletePlan(
            Supplier<Optional<FinancialTransactionPlanJpaEntity>> headerSupplier
    ) {
        Optional<FinancialTransactionPlanJpaEntity> financialTransactionPlanJpaEntityOptional =
                headerSupplier.get();
        if (financialTransactionPlanJpaEntityOptional.isEmpty()) {
            return Optional.empty();
        }

        FinancialTransactionPlanJpaEntity financialTransactionPlanJpaEntity =
                financialTransactionPlanJpaEntityOptional.get();
        Long planId = financialTransactionPlanJpaEntity.getId();
        List<FinancialTransactionJpaEntity> directFinancialTransactionJpaEntityList =
                financialTransactionJpaRepository.findDirectPlanComponentList(
                        FinancialTransactionSourceType.PLAN,
                        planId
                );
        List<InstallmentPlanTransactionJpaEntity> installmentPlanTransactionJpaEntityList =
                installmentPlanTransactionJpaRepository
                        .findDirectPlanComponentListWithInstallments(
                                FinancialTransactionSourceType.PLAN,
                                planId
                        );
        Map<Long, InstallmentPlanTransactionJpaEntity> installmentPlanTransactionJpaEntityMap =
                installmentPlanTransactionJpaEntityList.stream()
                        .collect(Collectors.toMap(
                                InstallmentPlanTransactionJpaEntity::getId,
                                installmentPlanTransactionJpaEntity ->
                                        installmentPlanTransactionJpaEntity
                        ));
        List<FinancialTransaction> financialTransactionList =
                directFinancialTransactionJpaEntityList.stream()
                        .map(financialTransactionJpaEntity -> resolveCompleteEntity(
                                financialTransactionJpaEntity,
                                installmentPlanTransactionJpaEntityMap
                        ))
                        .map(FinancialTransactionPersistenceMapper::toDomain)
                        .toList();
        return Optional.of(FinancialTransactionPlanPersistenceMapper.toDomain(
                financialTransactionPlanJpaEntity,
                financialTransactionList
        ));
    }

    private FinancialTransactionJpaEntity resolveCompleteEntity(
            FinancialTransactionJpaEntity financialTransactionJpaEntity,
            Map<Long, InstallmentPlanTransactionJpaEntity>
                    installmentPlanTransactionJpaEntityMap
    ) {
        if (!(financialTransactionJpaEntity
                instanceof InstallmentPlanTransactionJpaEntity installmentPlanTransactionJpaEntity)) {
            return financialTransactionJpaEntity;
        }
        return installmentPlanTransactionJpaEntityMap.getOrDefault(
                installmentPlanTransactionJpaEntity.getId(),
                installmentPlanTransactionJpaEntity
        );
    }
}
