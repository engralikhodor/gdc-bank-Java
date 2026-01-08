package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.TransactionDTO;
import com.alikhdr.bankingApp.dto.TransactionResponseDTO;
import com.alikhdr.bankingApp.dto.TransactionSearchDTO;

import java.util.List;

public interface TransactionService
{
    void saveTransaction(TransactionDTO transactionDTO);

    public List<TransactionResponseDTO> searchTransactions(TransactionSearchDTO searchDTO);
}
