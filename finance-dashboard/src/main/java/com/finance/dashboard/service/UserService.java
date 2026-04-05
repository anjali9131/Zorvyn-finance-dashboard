package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.UserUpdateRequest;
import com.finance.dashboard.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    UserResponse getCurrentUser(String username);
}
