package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.TransactionDTO;
import com.alikhdr.bankingApp.entity.Transaction;
import com.alikhdr.bankingApp.entity.TransactionStatusOptions;
import com.alikhdr.bankingApp.repository.TransactionRepository;
import com.alikhdr.bankingApp.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Generates constructor for final fields
public class TransactionImpl implements TransactionService
{
    private final TransactionRepository transactionRepository;

    @Transactional // non-negotiable in finTech. If the system crashes mid-save, it rolls back
    public void saveTransaction(TransactionDTO transactionDTO)
    {
        transactionRepository.save(Transaction.builder()
                .amount(transactionDTO.amount())
                .transactionType(transactionDTO.transactionType())
                .accountNumber(transactionDTO.accountNumber())
                .status(TransactionStatusOptions.PENDING)
                .build());
    }
}
