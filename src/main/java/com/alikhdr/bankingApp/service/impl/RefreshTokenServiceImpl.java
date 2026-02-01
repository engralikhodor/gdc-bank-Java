package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.entity.Auth;
import com.alikhdr.bankingApp.entity.RefreshToken;
import com.alikhdr.bankingApp.repository.AuthRepository;
import com.alikhdr.bankingApp.repository.RefreshTokenRepository;
import com.alikhdr.bankingApp.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService
{
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthRepository authRepository;

    @Override
    public RefreshToken createRefreshToken(String username)
    {
        Auth auth = authRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ensure only one active refresh token per user session
        refreshTokenRepository.deleteByAuth(auth);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .auth(auth)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(86400000)) // 24 hours
                .build();

        return refreshTokenRepository.save(newRefreshToken);
    }

    @Override
    public RefreshToken validateTokenExpiration(RefreshToken token)
    { // Renamed for clarity
        if (token.getExpiryDate().isBefore(Instant.now()))
        {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }
        return token;
    }
}
