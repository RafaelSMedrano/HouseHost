package com.househost.observability.application.port.in;

import com.househost.observability.application.dto.ClientLogRequestDTO;
import com.househost.observability.application.records.ClientLogRequestContextRecord;

public interface ClientLogUseCase {

    void record(ClientLogRequestDTO request, ClientLogRequestContextRecord contextRecord);
}
