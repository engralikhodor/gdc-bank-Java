package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController
{
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<GlobalResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request)
    {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @PostMapping
    public ResponseEntity<GlobalResponse<Void>> createTransaction(@Valid @RequestBody TransactionRequest request)
    {
        transactionService.saveTransaction(request);
        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .responseCode("201")
                .responseMessage("Transaction recorded successfully")
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalResponse<List<TransactionResponse>>> search(@Valid @ModelAttribute TransactionSearchCriteria criteria)
    {
        return ResponseEntity.ok(transactionService.searchTransactions(criteria));
    }
}
