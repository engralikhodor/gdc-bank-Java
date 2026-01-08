package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.TransactionTypeOptions;
import lombok.Builder;

import java.math.BigDecimal;

// (Request): for create
@Builder
public record TransactionRequest
        (
                TransactionTypeOptions transactionType,
                String accountNumber,
                String status,
                BigDecimal amount,
                String remarks
        )
{
}
