package com.campusreporter.service;

import com.campusreporter.dto.AuthResponseDTO;
import com.campusreporter.dto.LoginRequestDTO;
import com.campusreporter.entity.User;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO request);

    AuthResponseDTO getMe(String token);

    void logout(String token);

    User validateToken(String token);
    void invalidateUserSessions(Long userId);
}
