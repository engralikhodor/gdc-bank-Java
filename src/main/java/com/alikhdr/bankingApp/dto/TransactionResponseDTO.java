package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.TransactionTypeOptions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// (Response): Used for Returning data, including system-generated fields
public record TransactionResponseDTO(
        UUID id,
        TransactionTypeOptions transactionType,
        String accountNumber,
        String status,
        BigDecimal amount,
        String remarks,
        LocalDateTime createdAt
)
{
}
