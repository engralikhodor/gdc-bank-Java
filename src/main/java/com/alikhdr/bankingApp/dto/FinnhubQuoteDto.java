package com.alikhdr.bankingApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinnhubQuoteDto
        // Maps the raw data from Finnhub to a Java object
{
    private BigDecimal c; // Current price
    private BigDecimal h; // High price of the day
    private BigDecimal l; // Low price of the day
    private BigDecimal o; // Open price of the day
    private BigDecimal pc; // Previous close price
    private Long t;     // Timestamp
}
