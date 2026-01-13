package com.alikhdr.bankingApp.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String firstName,
        String accountNumber
)
{
}
