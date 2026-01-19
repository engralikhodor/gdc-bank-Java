package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.TransactionTypeOptions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

// (Request): for create
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest
{
    @NotNull(message = "Transaction type is required.")
    private TransactionTypeOptions transactionType;

    @NotBlank(message = "Destination account number is required.")
    private String destinationAccountNumber;

    @NotBlank(message = "Status is required.")
    private String status;

    @NotNull(message = "Amount is required.")
    @Positive(message = "The amount must be greater than zero.")
    private BigDecimal amount;

    private String remarks;//optional

    @NotNull(message = "Customer ID is required.")
    private UUID customerID;
}
