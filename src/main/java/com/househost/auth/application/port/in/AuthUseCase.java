package com.househost.auth.application.port.in;
import com.househost.auth.application.dto.*;
import com.househost.auth.application.records.LoginRequestContextRecord;
public interface AuthUseCase {
    LoginResponseDTO login(LoginRequestDTO request, LoginRequestContextRecord context);
    RegistrationResponseDTO registration(RegistrationRequestDTO request);
}
