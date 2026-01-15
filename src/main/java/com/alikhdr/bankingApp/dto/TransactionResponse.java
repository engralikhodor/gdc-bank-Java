package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.TransactionTypeOptions;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
// (Response): return data
public record TransactionResponse
        (
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
