package com.alikhdr.bankingApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest
{
    @NotBlank(message = "The sender's account number can't be empty.")
    private String sourceAccountNumber;

    @NotBlank(message = "The recipient's account number can't be empty.")
    private String destinationAccountNumber;

    private String remarks;

    @NotNull(message = "The amount can't be empty.")
    @Positive(message = "The amount must be greater than zero.")
    private BigDecimal amountToTransfer;
}
