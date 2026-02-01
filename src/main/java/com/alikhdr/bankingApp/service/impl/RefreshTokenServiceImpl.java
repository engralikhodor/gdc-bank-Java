package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.entity.Auth;
import com.alikhdr.bankingApp.entity.RefreshToken;
import com.alikhdr.bankingApp.repository.AuthRepository;
import com.alikhdr.bankingApp.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl
{
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthRepository authRepository;

    public RefreshToken createRefreshToken(String username)
    {
        Auth auth = authRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // first, delete any old accessToken
        refreshTokenRepository.deleteByAuth(auth);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .auth(auth)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(86400000))// 24 hours
                .build();

        return refreshTokenRepository.save(newRefreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token)
    {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0)
        {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh accessToken was expired. Please make a new sign-in request.");
        }
        return token;
    }
}
