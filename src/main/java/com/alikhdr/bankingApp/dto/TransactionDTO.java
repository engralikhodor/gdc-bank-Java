package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.TransactionTypeOptions;
import lombok.Builder;

import java.math.BigDecimal;

// (Request): Used for Creating a transaction
@Builder
public record TransactionDTO(TransactionTypeOptions transactionType,
                             String accountNumber,
                             String status,
                             BigDecimal amount,
                             String remarks)
{
}
