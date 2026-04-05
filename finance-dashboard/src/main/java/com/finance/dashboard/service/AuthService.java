package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.AuthRequest;
import com.finance.dashboard.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(AuthRequest.Register request);
    AuthResponse login(AuthRequest.Login request);
}
