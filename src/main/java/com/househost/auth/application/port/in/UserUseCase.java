package com.househost.auth.application.port.in;

import com.househost.auth.application.dto.UserResponseDTO;
import com.househost.auth.application.dto.UserPhotoRequestDTO;
import com.househost.auth.application.dto.UserProfileUpdateRequestDTO;
import java.util.List;

public interface UserUseCase {
    UserResponseDTO findByEmail(String email);
    List<UserResponseDTO> quickAccessUsers();
    UserResponseDTO updateUserPhoto(Long id, UserPhotoRequestDTO request);
    UserResponseDTO updateUserProfile(Long id, UserProfileUpdateRequestDTO request);
}
