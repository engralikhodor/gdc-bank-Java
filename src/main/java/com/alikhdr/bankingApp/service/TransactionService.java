package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.TransactionRequest;
import com.alikhdr.bankingApp.dto.TransactionResponse;
import com.alikhdr.bankingApp.dto.TransactionSearchCriteria;
import com.alikhdr.bankingApp.dto.TransferRequest;

import java.util.List;

public interface TransactionService
{
    void saveTransaction(TransactionRequest request);

    List<TransactionResponse> searchTransactions(TransactionSearchCriteria searchDTO);

    TransactionResponse transferAmount(TransferRequest request);
}
