package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.GlobalResponse;
import com.alikhdr.bankingApp.dto.TransactionResponse;
import com.alikhdr.bankingApp.dto.TransactionSearchCriteria;
import com.alikhdr.bankingApp.dto.TransferRequest;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController
{
    private final TransactionService transactionService;

    @GetMapping("/search")
    public GlobalResponse<List<TransactionResponse>> searchTransactions(@ModelAttribute TransactionSearchCriteria criteria)
    {
        List<TransactionResponse> results = transactionService.searchTransactions(criteria);

        return GlobalResponse.<List<TransactionResponse>>builder()
                .responseCode(results.isEmpty() ? AccountUtils.TRANSACTION_NOT_FOUND_CODE : "200")
                .responseMessage(results.isEmpty() ? AccountUtils.TRANSACTION_NOT_FOUND : "Search successful")
                .data(results)
                .build();
    }

    @PostMapping("/transfer")
    public GlobalResponse<TransactionResponse> transferAmount(@Valid @RequestBody TransferRequest request)
    {
        TransactionResponse data = transactionService.transferAmount(request);

        return GlobalResponse.<TransactionResponse>builder()
                .responseCode(AccountUtils.AMOUNT_TRANSFERRED_CODE)
                .responseMessage(AccountUtils.AMOUNT_TRANSFERRED_SUCCESSFULLY)
                .data(data)
                .build();
    }
}
