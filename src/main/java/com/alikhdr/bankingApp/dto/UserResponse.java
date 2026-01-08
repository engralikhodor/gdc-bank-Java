package com.alikhdr.bankingApp.dto;

import lombok.Builder;

import java.math.BigDecimal;

// (Response): To return data, including system-generated fields
// for sure, we don't add `version` and sensitive columns here
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
