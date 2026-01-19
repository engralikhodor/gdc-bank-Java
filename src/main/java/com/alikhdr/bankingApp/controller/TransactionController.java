package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.GlobalResponse;
import com.alikhdr.bankingApp.dto.TransactionResponse;
import com.alikhdr.bankingApp.dto.TransactionSearchCriteria;
import com.alikhdr.bankingApp.dto.TransferRequest;
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

    @PostMapping("/transfer")
    public ResponseEntity<GlobalResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request)
    {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @PostMapping("/search")
    public ResponseEntity<List<TransactionResponse>> search(@RequestBody TransactionSearchCriteria criteria)
    {
        return ResponseEntity.ok(transactionService.searchTransactions(criteria));
    }
}
