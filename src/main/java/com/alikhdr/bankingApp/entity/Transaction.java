package com.alikhdr.bankingApp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "transaction")

public class Transaction
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(java.sql.Types.VARCHAR)
    @Column(updatable = false, nullable = false, length = 36)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionTypeOptions transactionType;

    @Column(nullable = false, length = 10)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatusOptions status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version; // Optimistic Locking: protect the State of the record (Best Practice)
}
