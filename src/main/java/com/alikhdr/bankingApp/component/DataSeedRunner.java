package com.alikhdr.bankingApp.component;

import com.alikhdr.bankingApp.constants.ResponseConstants;
import com.alikhdr.bankingApp.dto.TransactionRequest;
import com.alikhdr.bankingApp.entity.*;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataSeedRunner implements CommandLineRunner
{

    private final CustomerRepository customerRepository;
    private final TransactionService transactionService;
    private final PasswordEncoder passwordEncoder; // Correctly encode passwords for login

    @Override
    public void run(String... args) throws Exception
    {
        if (customerRepository.count() > 0)
        {
            return;
        }

        Customer charbel = Customer.builder()
                .firstName("Charbel")
                .lastName("Mansour")
                .email("charbel.m@bank.lb")
                .phoneNumber("96103123456")
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(new BigDecimal("15000.00"))
                .gender(GenderOptions.MALE)
                .status(AccountStatusOptions.ACTIVE)
                .baseCurrency(CurrencyOptions.USD)
                .dailyTransferLimit(ResponseConstants.DEFAULT_TRANSFER_LIMIT) // Changed
                .dateOfBirth(LocalDate.parse("1990-01-01"))
                .governmentId("125898888")
                .nationality("Lebanese")
                .occupation("Teacher")
                .build();

        Auth charbelAuth = new Auth();
        charbelAuth.setUsername("charbel_m");
        charbelAuth.setPassword(passwordEncoder.encode("password123"));
        charbelAuth.setRole(RoleOptions.CUSTOMER);

        charbelAuth.setCustomer(charbel);
        charbel.setAuth(charbelAuth);

        Customer laila = Customer.builder()
                .firstName("Laila")
                .lastName("Khoury")
                .email("laila.k@bank.lb")
                .phoneNumber("96171987654")
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(new BigDecimal("9000.00"))
                .gender(GenderOptions.FEMALE)
                .status(AccountStatusOptions.ACTIVE)
                .baseCurrency(CurrencyOptions.USD)
                .dailyTransferLimit(ResponseConstants.DEFAULT_TRANSFER_LIMIT) // Changed
                .dateOfBirth(LocalDate.parse("1980-08-08"))
                .governmentId("888812589")
                .nationality("Swedish")
                .occupation("CEO")
                .build();

        Auth lailaAuth = new Auth();
        lailaAuth.setUsername("laila_k"); // Fixed username
        lailaAuth.setPassword(passwordEncoder.encode("password123"));
        lailaAuth.setRole(RoleOptions.CUSTOMER);

        lailaAuth.setCustomer(laila);
        laila.setAuth(lailaAuth);

        // save Auth objects: by Cascade
        customerRepository.saveAll(List.of(charbel, laila));

        // generate 30 Transactions for Charbel
        Random random = new Random();
        for (int i = 0; i < 30; i++)
        {
            BigDecimal amount = BigDecimal.valueOf(20 + (1480 * random.nextDouble()));
            TransactionTypeOptions type = random.nextBoolean() ?
                    TransactionTypeOptions.CREDIT : TransactionTypeOptions.DEBIT;

            transactionService.saveTransaction(TransactionRequest.builder()
                    .destinationAccountNumber(charbel.getAccountNumber())
                    .amount(amount)
                    .transactionType(type)
                    .status(TransactionStatusOptions.COMPLETED.name())
                    .remarks("Seed Transaction " + (i + 1))
                    .customerID(charbel.getId())
                    .build());
        }

        System.out.println("🇱🇧 Database successfully seeded with Lebanese profiles and 30 transactions.");
    }
}
