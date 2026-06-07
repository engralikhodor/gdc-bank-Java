package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.*;

public interface AuthService
{
    AuthResponse register(AuthRegisterRequest authRegisterRequest);

    AuthResponse login(AuthLoginRequest authLoginRequest);

    AuthResponse refreshToken(TokenRefreshRequest request);
}
