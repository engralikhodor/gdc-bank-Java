package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.*;

public interface AuthService
{
    GlobalResponse<AuthResponse> register(AuthRegisterRequest authRegisterRequest);

    GlobalResponse<AuthResponse> login(AuthLoginRequest authLoginRequest);

    GlobalResponse<AuthResponse> refreshToken(TokenRefreshRequest request);
}
