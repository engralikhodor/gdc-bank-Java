package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.TransactionDTO;
import com.alikhdr.bankingApp.dto.TransactionResponseDTO;
import com.alikhdr.bankingApp.dto.TransactionSearchDTO;
import com.alikhdr.bankingApp.entity.Transaction;
import com.alikhdr.bankingApp.entity.TransactionStatusOptions;
import com.alikhdr.bankingApp.entity.Transaction_;
import com.alikhdr.bankingApp.mapper.TransactionMapper;
import com.alikhdr.bankingApp.repository.TransactionRepository;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.specs.TransactionSpecs;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Generates constructor for final fields
public class TransactionImpl implements TransactionService
{
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional // non-negotiable in finTech. If the system crashes mid-save, it rolls back
    public void saveTransaction(TransactionDTO transactionDTO)
    {
        transactionRepository.save(Transaction.builder()
                .amount(transactionDTO.amount())
                .transactionType(transactionDTO.transactionType())
                .accountNumber(transactionDTO.accountNumber())
                .status(TransactionStatusOptions.PENDING)
                .remarks(transactionDTO.remarks())
                .build());
    }

    @Override
    public List<TransactionResponseDTO>
    searchTransactions(TransactionSearchDTO searchDTO)
    {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecs.isEquals(Transaction_.TRANSACTION_TYPE, searchDTO.getType()))
                .and(TransactionSpecs.isEquals(Transaction_.STATUS, searchDTO.getStatus()))
                .and(TransactionSpecs.containsText(Transaction_.REMARKS, searchDTO.getRemarks()))
                .and(TransactionSpecs.amountBetween(searchDTO.getMinAmount(), searchDTO.getMaxAmount()));

        return transactionRepository.findAll(spec)
                .stream()
                .map(transactionMapper::entityToDto)
                .collect(Collectors.toList());
    }
}
