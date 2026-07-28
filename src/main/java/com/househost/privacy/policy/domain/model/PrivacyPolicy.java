package com.househost.privacy.policy.domain.model;

import com.househost.shared.exception.PrivacyException;
import java.time.LocalDateTime;

public class PrivacyPolicy {
    private Long id;
    private int version;
    private String title;
    private String content;
    private PrivacyPolicyContentHash contentHash;
    private PrivacyPolicyStatus status;
    private LocalDateTime effectiveAt;
    private LocalDateTime publishedAt;
    private Long publishedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PrivacyPolicy(
            int version,
            String title,
            String content,
            LocalDateTime effectiveAt
    ) {
        this.version = version;
        this.title = title;
        this.content = content;
        this.effectiveAt = effectiveAt;
        this.status = PrivacyPolicyStatus.DRAFT;
    }

    public void updateDraft(String title, String content, LocalDateTime effectiveAt) {
        requireDraft("Somente uma politica em rascunho pode ser alterada.");
        this.title = title;
        this.content = content;
        this.effectiveAt = effectiveAt;
        this.contentHash = null;
        touch();
    }

    public void publish(
            PrivacyPolicyContentHash contentHash,
            Long publisherUserId,
            LocalDateTime publicationTime
    ) {
        requireDraft("Somente uma politica em rascunho pode ser publicada.");
        if (publisherUserId == null) {
            throw new PrivacyException("Usuario publicador nao identificado.");
        }
        this.contentHash = contentHash;
        this.status = PrivacyPolicyStatus.PUBLISHED;
        this.publishedByUserId = publisherUserId;
        this.publishedAt = publicationTime;
        this.updatedAt = publicationTime;
    }

    public void supersede(LocalDateTime supersededAt) {
        if (status != PrivacyPolicyStatus.PUBLISHED) {
            throw new PrivacyException("Somente a politica publicada pode ser substituida.");
        }
        status = PrivacyPolicyStatus.SUPERSEDED;
        updatedAt = supersededAt;
    }

    public void prepareForCreation() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    public void restorePersistenceState(
            Long id,
            PrivacyPolicyContentHash contentHash,
            PrivacyPolicyStatus status,
            LocalDateTime publishedAt,
            Long publishedByUserId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.contentHash = contentHash;
        this.status = status;
        this.publishedAt = publishedAt;
        this.publishedByUserId = publishedByUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private void requireDraft(String message) {
        if (status != PrivacyPolicyStatus.DRAFT) {
            throw new PrivacyException(message);
        }
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public PrivacyPolicyContentHash getContentHash() {
        return contentHash;
    }

    public PrivacyPolicyStatus getStatus() {
        return status;
    }

    public LocalDateTime getEffectiveAt() {
        return effectiveAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public Long getPublishedByUserId() {
        return publishedByUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
