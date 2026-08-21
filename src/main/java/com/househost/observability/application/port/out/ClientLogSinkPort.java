package com.househost.observability.application.port.out;

import com.househost.observability.application.records.SanitizedClientLogRecord;

public interface ClientLogSinkPort {

    void emit(SanitizedClientLogRecord clientLogRecord);
}
