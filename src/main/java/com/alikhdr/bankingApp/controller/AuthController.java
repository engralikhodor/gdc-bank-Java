package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.AuthLoginRequest;
import com.alikhdr.bankingApp.dto.AuthRegisterRequest;
import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.dto.GlobalResponse;
import com.alikhdr.bankingApp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthController
{
    private final AuthService authService;

    @PostMapping("/register")
    public GlobalResponse<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest authRegisterRequest)
    {
        return authService.register(authRegisterRequest);
    }

    @PostMapping("/login")
    public GlobalResponse<AuthResponse> login(@Valid @RequestBody AuthLoginRequest authLoginRequest)
    {
        return authService.login(authLoginRequest);
    }
}
