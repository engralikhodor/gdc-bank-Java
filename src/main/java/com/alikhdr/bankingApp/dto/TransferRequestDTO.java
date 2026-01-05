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
public class TransferRequestDTO
{
    @NotBlank(message = "The sender's account number can't be empty.")
    private String fromAccountNumber;

    @NotBlank(message = "The recipient's account number can't be empty.")
    private String toAccountNumber;

    @NotNull(message = "The amount can't be empty.")
    @Positive(message = "The amount must be greater than zero.")
    private BigDecimal amountToTransfer;
}
