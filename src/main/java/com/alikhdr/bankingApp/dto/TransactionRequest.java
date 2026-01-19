package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.TransactionTypeOptions;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

// (Request): for create
@Builder
public record TransactionRequest(
        TransactionTypeOptions transactionType,
        String destinationAccountNumber,
        String status,
        BigDecimal amount,
        String remarks,
        UUID customerId // Changed from String to UUID
)
{
}
