package com.alikhdr.bankingApp.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String username,
        String firstName,
        String accountNumber,
        String accessToken,
        String refreshToken // when a user logs in, they need to receive both the JWT and the Refresh Token
)
{
}
