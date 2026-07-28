package com.househost.privacy.policy.application.dto;

import java.time.LocalDateTime;

public class PrivacyPolicyRequestDTO {
    public Integer version;
    public String title;
    public String content;
    public LocalDateTime effectiveAt;
}
