package com.campusreporter.service;

import com.campusreporter.dto.CreateUserRequestDTO;
import com.campusreporter.dto.UserResponseDTO;
import java.util.List;

public interface UserService {

    List<UserResponseDTO> getAllUsers(String authHeader);

    UserResponseDTO createUser(CreateUserRequestDTO request, String authHeader);

    UserResponseDTO deactivateUser(Long userId, String authHeader);
}
