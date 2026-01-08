package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.TransactionTypeOptions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// (Response): To return data, including system-generated fields
// for sure, we don't add `version` and sensitive columns here
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
