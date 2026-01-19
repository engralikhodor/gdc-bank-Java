package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController
{

    private final TransactionService transactionService;

    // Existing Transfer endpoint
    @PostMapping("/transfer")
    public ResponseEntity<GlobalResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request)
    {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    // NEW: Manual Transaction Creation (using your updated TransactionRequest)
    @PostMapping
    public ResponseEntity<GlobalResponse<Void>> createTransaction(@Valid @RequestBody TransactionRequest request)
    {
        transactionService.saveTransaction(request);
        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .responseCode("201")
                .responseMessage("Transaction recorded successfully")
                .build());
    }

    @PostMapping("/search")
    public ResponseEntity<List<TransactionResponse>> search(@RequestBody TransactionSearchCriteria criteria)
    {
        return ResponseEntity.ok(transactionService.searchTransactions(criteria));
    }
}
