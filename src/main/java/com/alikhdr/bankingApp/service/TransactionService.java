package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.TransactionDTO;

public interface TransactionService
{
    void saveTransaction(TransactionDTO transactionDTO);
}
