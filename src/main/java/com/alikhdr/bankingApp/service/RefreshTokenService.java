package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.entity.RefreshToken;

public interface RefreshTokenService
{
    RefreshToken createRefreshToken(String username);

    RefreshToken validateTokenExpiration(RefreshToken token);
}
