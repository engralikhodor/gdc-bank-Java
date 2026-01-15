package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.TransactionRequest;
import com.alikhdr.bankingApp.dto.TransactionResponse;
import com.alikhdr.bankingApp.dto.TransactionSearchCriteria;
import com.alikhdr.bankingApp.dto.TransferRequest;
import com.alikhdr.bankingApp.entity.*;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionImpl implements TransactionService
{
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final CustomerRepository customerRepository;

    @Override
    public void saveTransaction(TransactionRequest request)
    {
        Transaction transaction = Transaction.builder()
                .transactionType(request.transactionType())
                .accountNumber(request.accountNumber())
                .amount(request.amount())
                .status(TransactionStatusOptions.valueOf(request.status()))
                .remarks(request.remarks())
                .build();

        transactionRepository.save(transaction);
    }

    @Override
    public List<TransactionResponse> searchTransactions(TransactionSearchCriteria searchDTO)
    {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecs.isEquals(Transaction_.ACCOUNT_NUMBER, searchDTO.accountNumber()))
                .and(TransactionSpecs.isType(searchDTO.transactionType()));

        return transactionRepository.findAll(spec)
                .stream()
                .map(transactionMapper::entityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse transferAmount(TransferRequest transferRequest)
    {
        if (transferRequest.getFromAccountNumber().equals(transferRequest.getToAccountNumber()))
        {
            throw new SameAccountTransferException();
        }

        Customer fromCustomer = customerRepository.findByAccountNumber(transferRequest.getFromAccountNumber());
        Customer toCustomer = customerRepository.findByAccountNumber(transferRequest.getToAccountNumber());

        if (fromCustomer == null || toCustomer == null)
        {
            throw new AccountNotFoundException();
        }

        BigDecimal amount = transferRequest.getAmountToTransfer();

        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            throw new ExceedsTransferLimitException();
        }

        if (fromCustomer.getAccountBalance().compareTo(amount) < 0)
        {
            throw new InsufficientResourcesException();
        }

        executeBalanceUpdate(fromCustomer.getAccountNumber(), amount, TransactionTypeOptions.DEBIT, transferRequest.getRemarks());
        executeBalanceUpdate(toCustomer.getAccountNumber(), amount, TransactionTypeOptions.CREDIT, transferRequest.getRemarks());

        log.info("Transfer successful: {} moved from {} to {}", amount, fromCustomer.getAccountNumber(), toCustomer.getAccountNumber());

        return TransactionResponse.builder()
                .amount(amount)
                .accountNumber(fromCustomer.getAccountNumber())
                .build();
    }

    private void executeBalanceUpdate(String accountNumber, BigDecimal amount, TransactionTypeOptions type, String remarks)
    {
        Customer customer = customerRepository.findByAccountNumber(accountNumber);

        if (type == TransactionTypeOptions.CREDIT)
        {
            customer.setAccountBalance(customer.getAccountBalance().add(amount));
        }
        else
        {
            customer.setAccountBalance(customer.getAccountBalance().subtract(amount));
        }

        customerRepository.save(customer);

        this.saveTransaction(TransactionRequest.builder()
                .amount(amount)
                .accountNumber(accountNumber)
                .transactionType(type)
                .status(TransactionStatusOptions.COMPLETED.name())
                .remarks(remarks)
                .build());
    }
}
