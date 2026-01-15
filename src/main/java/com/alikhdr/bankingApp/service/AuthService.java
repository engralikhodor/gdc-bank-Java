package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.AuthLoginRequest;
import com.alikhdr.bankingApp.dto.AuthRegisterRequest;
import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.dto.GlobalResponse;

public interface AuthService
{
    GlobalResponse<AuthResponse> register(AuthRegisterRequest authRegisterRequest);

    GlobalResponse<AuthResponse> login(AuthLoginRequest authLoginRequest);
}
