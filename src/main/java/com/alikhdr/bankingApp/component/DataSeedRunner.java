package com.alikhdr.bankingApp.component;

import com.alikhdr.bankingApp.dto.TransactionRequest;
import com.alikhdr.bankingApp.entity.*;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
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

    @Override
    public void run(String... args) throws Exception
    {
        // Prevent duplicate seeding
        if (customerRepository.count() > 0)
        {
            return;
        }

        // create (Lebanese) customers
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
                .dailyTransferLimit(AccountUtils.DEFAULT_TRANSFER_LIMIT)
                .dateOfBirth(LocalDate.parse("1990-01-01"))
                .governmentId("125898888")
                .nationality("Lebanese")
                .occupation("Teacher")
                .build();

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
                .dailyTransferLimit(AccountUtils.DEFAULT_TRANSFER_LIMIT)
                .dateOfBirth(LocalDate.parse("1980-08-08"))
                .governmentId("888812589")
                .nationality("Swedish")
                .occupation("CEO")
                .build();

        //  create and link the Auth object
        Auth charbelAuth = new Auth();
        charbelAuth.setUsername("charbel_m");
        charbelAuth.setPassword("encoded_password");
        charbelAuth.setRole(RoleOptions.CUSTOMER);
        charbelAuth.setCustomer(charbel);
        charbel.setAuth(charbelAuth);

        //  create and link the Auth object
        Auth lailaAuth = new Auth();
        lailaAuth.setUsername("charbel_m");
        lailaAuth.setPassword("encoded_password");
        lailaAuth.setRole(RoleOptions.CUSTOMER);
        lailaAuth.setCustomer(charbel);
        charbel.setAuth(lailaAuth);

        customerRepository.saveAll(List.of(charbel, laila));

        // Generate 30 Transactions for Charbel
        Random random = new Random();
        TransactionStatusOptions[] statuses = {
                TransactionStatusOptions.COMPLETED,
                TransactionStatusOptions.PENDING
        };

        for (int i = 0; i < 30; i++)
        {
            BigDecimal amount = BigDecimal.valueOf(20 + (1480 * random.nextDouble()));
            TransactionTypeOptions type = random.nextBoolean() ?
                    TransactionTypeOptions.CREDIT : TransactionTypeOptions.DEBIT;

            transactionService.saveTransaction(TransactionRequest.builder()
                    .destinationAccountNumber(charbel.getAccountNumber())
                    .amount(amount)
                    .transactionType(type)
                    .status(String.valueOf(statuses[random.nextInt(statuses.length)]))
                    .customerId(charbel.getId()) // Add the ID so the service can link the transaction to Charbel
                    .build());
        }

        System.out.println("🇱🇧 Database successfully seeded with Lebanese profiles and 30 transactions.");
    }
}
