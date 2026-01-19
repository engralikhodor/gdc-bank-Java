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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
        Customer customer = customerRepository.findById(request.getCustomerID())
                .orElseThrow(AccountNotFoundException::new);

        Transaction transaction = Transaction.builder()
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .status(TransactionStatusOptions.valueOf(request.getStatus()))
                .remarks(request.getRemarks())
                .customer(customer)
                .build();

        transactionRepository.save(transaction);
    }

    @Override
    public GlobalResponse<List<TransactionResponse>> searchTransactions(TransactionSearchCriteria searchDTO)
    {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecs.hasAccountNumber(searchDTO.accountNumber()))
                .and(TransactionSpecs.isType(searchDTO.transactionType()));

        List<TransactionResponse> results = transactionRepository.findAll(spec)
                .stream()
                .map(transactionMapper::entityToResponse)
                .collect(Collectors.toList());

        return GlobalResponse.<List<TransactionResponse>>builder()
                .responseCode(String.valueOf(HttpStatus.OK.value()))
                .responseMessage(results.isEmpty() ? "No transactions found matching criteria" : "Transactions retrieved successfully")
                .data(results)
                .build();
    }

    @Override
    @Transactional // ensure either both accounts update or none
    public GlobalResponse<TransactionResponse> transfer(TransferRequest request)
    {
        // validate same account transfer
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber()))
        {
            throw new SameAccountTransferException();
        }

        // fetch accounts
        Customer senderCustomer = customerRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(AccountNotFoundException::new);

        Customer receiverCustomer = customerRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(AccountNotFoundException::new);

        BigDecimal amount = request.getAmountToTransfer();

        // check if senderCustomer balance < amount
        if (senderCustomer.getAccountBalance().compareTo(amount) < 0)
        {
            throw new InsufficientResourcesException();
        }

        // check if senderCustomer balance < DailyTransferLimi
        if (amount.compareTo(senderCustomer.getDailyTransferLimit()) > 0)
        {
            throw new ExceedsTransferLimitException();
        }

        // update both balances
        executeBalanceUpdate(senderCustomer, amount, TransactionTypeOptions.DEBIT, request.getRemarks());
        executeBalanceUpdate(receiverCustomer, amount, TransactionTypeOptions.CREDIT, request.getRemarks());

        log.info("Transfer successful: {} from {} to {}", amount, senderCustomer.getAccountNumber(), receiverCustomer.getAccountNumber());

        TransactionResponse data = TransactionResponse.builder()
                .amount(amount)
                .accountNumber(senderCustomer.getAccountNumber())
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

        this.saveTransaction(TransactionRequest.builder()
                .amount(amount)
                .destinationAccountNumber(customer.getAccountNumber())
                .transactionType(type)
                .status(TransactionStatusOptions.COMPLETED.name())
                .remarks(remarks)
                .customerID(customer.getId())
                .build());
    }
}
