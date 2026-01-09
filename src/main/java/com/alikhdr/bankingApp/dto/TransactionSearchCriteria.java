package com.alikhdr.bankingApp.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

// (Filter): search
@Data
public class TransactionSearchCriteria
{
    private String type;
    private String status;

    @PositiveOrZero(message = "Minimum amount cannot be negative")
    private BigDecimal minAmount;

    @PositiveOrZero(message = "Maximum amount cannot be negative")
    private BigDecimal maxAmount;

    private String remarks;
}
