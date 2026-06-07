package com.alikhdr.bankingApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditDebitRequest
{
    @NotBlank(message = "The account number can't be empty.")
    private String accountNumber;

    @NotNull(message = "The amount can't be empty.")
    @Positive(message = "The amount must be greater than zero.")
    private BigDecimal amount;
}
