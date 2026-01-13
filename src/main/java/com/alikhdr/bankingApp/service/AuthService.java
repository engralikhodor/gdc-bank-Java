package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.AuthRequest;
import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.dto.GlobalResponse;

public interface AuthService
{
    GlobalResponse<AuthResponse> register(AuthRequest authRequest);
}
