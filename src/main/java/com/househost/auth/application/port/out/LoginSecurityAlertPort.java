package com.househost.auth.application.port.out;

import com.househost.auth.application.records.LoginSecurityAlertMessageRecord;

public interface LoginSecurityAlertPort {
    void send(LoginSecurityAlertMessageRecord alert);
}
