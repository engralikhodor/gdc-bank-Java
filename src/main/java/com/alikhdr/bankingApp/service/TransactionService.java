package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.TransactionRequest;
import com.alikhdr.bankingApp.dto.TransactionResponse;
import com.alikhdr.bankingApp.dto.TransactionSearchCriteria;

import java.util.List;

public interface TransactionService
{
    void saveTransaction(TransactionRequest transactionRequest);

    List<TransactionResponse> searchTransactions(TransactionSearchCriteria searchDTO);

}
