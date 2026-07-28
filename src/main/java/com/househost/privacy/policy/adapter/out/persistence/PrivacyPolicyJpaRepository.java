package com.househost.privacy.policy.adapter.out.persistence;

import com.househost.privacy.policy.adapter.out.persistence.entity.PrivacyPolicyJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

interface PrivacyPolicyJpaRepository extends JpaRepository<PrivacyPolicyJpaEntity, Long> {
    Optional<PrivacyPolicyJpaEntity> findByVersion(Integer version);

    List<PrivacyPolicyJpaEntity> findAllByOrderByVersionDesc();

    Optional<PrivacyPolicyJpaEntity> findByCurrentSlot(String currentSlot);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PrivacyPolicyJpaEntity> findWithLockByCurrentSlot(String currentSlot);
}
