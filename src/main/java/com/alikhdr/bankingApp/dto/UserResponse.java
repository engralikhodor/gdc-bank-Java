package com.alikhdr.bankingApp.dto;

import lombok.Builder;

import java.math.BigDecimal;

// (Response): return data
@Builder
public record UserResponse
        (
                String accountName,
                String accountNumber,
                String phoneNumber,
                BigDecimal accountBalance
        )
{
}
