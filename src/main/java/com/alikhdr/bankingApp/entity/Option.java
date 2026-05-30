package com.alikhdr.bankingApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "options") // Renamed to avoid conflict with SQL keyword
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Option
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol; // ticker symbol (for ex: "AAPL")

    @Enumerated(EnumType.STRING)
    private OptionType type; // CALL or PUT

    private BigDecimal strikePrice;

    private LocalDate expirationDate;

    public enum OptionType
    {
        CALL,
        PUT
    }
}
