package com.alikhdr.bankingApp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "customer", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number")
})

public class Customer
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(java.sql.Types.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String otherName;// nullable (optional)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenderOptions gender;

    @Column(nullable = false)
    private String nationality;

    private String address;// nullable (optional)

    @Column(name = "account_number", unique = true, nullable = false, length = 10)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal accountBalance;

    @Column(nullable = false)
    private BigDecimal dailyTransferLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyOptions baseCurrency;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    private String alternativePhoneNumber;// nullable

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatusOptions status;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(unique = true, nullable = false)
    private String governmentId; // SSN, BVN, or national ID

    @Column(nullable = false)
    private String occupation;

    private boolean isEmailVerified = false;// nullable

    private LocalDateTime lastLogin;// nullable

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime modifiedAt;

    @Version
    private Long version; // For optimistic locking

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private Auth auth;
}
