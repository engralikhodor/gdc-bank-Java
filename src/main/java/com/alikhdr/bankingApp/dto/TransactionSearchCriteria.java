package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.TransactionTypeOptions;

public record TransactionSearchCriteria(
        String accountNumber,
        TransactionTypeOptions transactionType
)
{
}
