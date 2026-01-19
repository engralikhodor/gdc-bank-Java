package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.*;

import java.util.List;

public interface TransactionService
{
    void saveTransaction(TransactionRequest request);

    List<TransactionResponse> searchTransactions(TransactionSearchCriteria searchDTO);

    GlobalResponse<TransactionResponse> transfer(TransferRequest request);
}
