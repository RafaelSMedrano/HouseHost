package com.househost.privacy.policy.adapter.out.persistence;

import com.househost.privacy.policy.adapter.out.persistence.entity.PrivacyPolicyJpaEntity;
import com.househost.privacy.policy.adapter.out.persistence.entity.PrivacyPolicyPersistenceMapper;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyPersistencePort;
import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PrivacyPolicyPersistenceAdapter implements PrivacyPolicyPersistencePort {
    private final PrivacyPolicyJpaRepository repository;

    public PrivacyPolicyPersistenceAdapter(PrivacyPolicyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PrivacyPolicy save(PrivacyPolicy privacyPolicy) {
        prepareTimestamps(privacyPolicy);
        return map(repository.save(PrivacyPolicyPersistenceMapper.toEntity(privacyPolicy)));
    }

    @Override
    public PrivacyPolicy saveAndFlush(PrivacyPolicy privacyPolicy) {
        prepareTimestamps(privacyPolicy);
        return map(repository.saveAndFlush(PrivacyPolicyPersistenceMapper.toEntity(privacyPolicy)));
    }

    @Override
    public Optional<PrivacyPolicy> findById(Long id) {
        return repository.findById(id).map(PrivacyPolicyPersistenceMapper::toDomain);
    }

    @Override
    public Optional<PrivacyPolicy> findByVersion(int version) {
        return repository.findByVersion(version).map(PrivacyPolicyPersistenceMapper::toDomain);
    }

    @Override
    public List<PrivacyPolicy> findAllByVersionDescending() {
        return repository.findAllByOrderByVersionDesc().stream()
                .map(PrivacyPolicyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PrivacyPolicy> findCurrentPublished() {
        return repository.findByCurrentSlot("CURRENT")
                .map(PrivacyPolicyPersistenceMapper::toDomain);
    }

    @Override
    public Optional<PrivacyPolicy> findCurrentPublishedForUpdate() {
        return repository.findWithLockByCurrentSlot("CURRENT")
                .map(PrivacyPolicyPersistenceMapper::toDomain);
    }

    private PrivacyPolicy map(PrivacyPolicyJpaEntity policyJpaEntity) {
        return PrivacyPolicyPersistenceMapper.toDomain(policyJpaEntity);
    }

    private void prepareTimestamps(PrivacyPolicy privacyPolicy) {
        if (privacyPolicy.getCreatedAt() == null) {
            privacyPolicy.prepareForCreation();
        }
    }
}
