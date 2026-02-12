package com.alikhdr.bankingApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bill
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String billerName;
    private BigDecimal amount;
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private BillStatus status;// pending, paid, failed
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String receiptUrl;// AWS S3 link
}

