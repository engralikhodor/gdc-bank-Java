package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.entity.Customer;
import com.alikhdr.bankingApp.entity.Transaction;
import com.alikhdr.bankingApp.entity.TransactionStatusOptions;
import com.alikhdr.bankingApp.entity.TransactionTypeOptions;
import com.alikhdr.bankingApp.exception.AccountNotFoundException;
import com.alikhdr.bankingApp.exception.ExceedsTransferLimitException;
import com.alikhdr.bankingApp.exception.InsufficientResourcesException;
import com.alikhdr.bankingApp.exception.SameAccountTransferException;
import com.alikhdr.bankingApp.mapper.TransactionMapper;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.repository.TransactionRepository;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.specs.TransactionSpecs;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionImpl implements TransactionService
{

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public void saveTransaction(TransactionRequest request)
    {
        // Fetch the customer using the ID from the request
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(AccountNotFoundException::new);

        Transaction transaction = Transaction.builder()
                .transactionType(request.transactionType())
                .amount(request.amount())
                .destinationAccountNumber(request.destinationAccountNumber())
                .status(TransactionStatusOptions.valueOf(request.status()))
                .remarks(request.remarks())
                .customer(customer)
                .build();

        transactionRepository.save(transaction);
    }

    @Override
    public List<TransactionResponse> searchTransactions(TransactionSearchCriteria searchDTO)
    {
        Specification<Transaction> spec = TransactionSpecs.withCriteria(searchDTO);
        return transactionRepository.findAll(spec)
                .stream()
                .map(transactionMapper::entityToResponse)
                .toList();
    }

    @Override
    @Transactional // Ensures either both accounts update or none
    public GlobalResponse<TransactionResponse> transfer(TransferRequest request)
    {
        // 1. Validate same account transfer
        if (request.getFromAccountNumber().equals(request.getDestinationAccountNumber()))
        {
            throw new SameAccountTransferException();
        }

        // 2. Fetch Accounts
        Customer fromCustomer = customerRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(AccountNotFoundException::new);

        Customer toCustomer = customerRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(AccountNotFoundException::new);

        BigDecimal amount = request.getAmountToTransfer();

        // 3. Business Rule Validations
        if (fromCustomer.getAccountBalance().compareTo(amount) < 0)
        {
            throw new InsufficientResourcesException();
        }

        if (amount.compareTo(fromCustomer.getDailyTransferLimit()) > 0)
        {
            throw new ExceedsTransferLimitException();
        }

        // 4. Atomic Balance Updates
        executeBalanceUpdate(fromCustomer, amount, TransactionTypeOptions.DEBIT, request.getRemarks());
        executeBalanceUpdate(toCustomer, amount, TransactionTypeOptions.CREDIT, request.getRemarks());

        log.info("Transfer successful: {} from {} to {}", amount, fromCustomer.getAccountNumber(), toCustomer.getAccountNumber());

        TransactionResponse data = TransactionResponse.builder()
                .amount(amount)
                .accountNumber(fromCustomer.getAccountNumber())
                .build();

        return GlobalResponse.<TransactionResponse>builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESSFUL_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE)
                .data(data)
                .build();
    }

    private void executeBalanceUpdate(Customer customer, BigDecimal amount, TransactionTypeOptions type, String remarks)
    {
        if (type == TransactionTypeOptions.CREDIT)
        {
            customer.setAccountBalance(customer.getAccountBalance().add(amount));
        }
        else
        {
            customer.setAccountBalance(customer.getAccountBalance().subtract(amount));
        }

        customerRepository.save(customer);

        // Ensure the customerId is passed correctly here
        this.saveTransaction(TransactionRequest.builder()
                .amount(amount)
                .destinationAccountNumber(customer.getAccountNumber())
                .transactionType(type)
                .status(TransactionStatusOptions.COMPLETED.name())
                .remarks(remarks)
                .customerId(customer.getId()) // Ensure this matches your record field name
                .build());
    }
}
