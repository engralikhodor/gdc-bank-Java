package com.alikhdr.bankingApp.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String username,
        String token,
        String firstName,
        String accountNumber
)
{
}
