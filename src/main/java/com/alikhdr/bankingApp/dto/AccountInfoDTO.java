package com.alikhdr.bankingApp.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AccountInfoDTO(
        BigDecimal accountBalance,
        String accountName,
        String accountNumber)
{
}
