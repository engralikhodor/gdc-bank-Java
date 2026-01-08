package com.alikhdr.bankingApp.controller;


import com.alikhdr.bankingApp.dto.TransactionResponseDTO;
import com.alikhdr.bankingApp.dto.TransactionSearchDTO;
import com.alikhdr.bankingApp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController
{
    private final TransactionService transactionService;

    @GetMapping("/search")
    public ResponseEntity<List<TransactionResponseDTO>>
    searchTransactions(@ModelAttribute TransactionSearchDTO searchCriteria)  //@ModelAttribute used for mapping request => object
    {
        List<TransactionResponseDTO> results =
                transactionService.searchTransactions(searchCriteria);
        return ResponseEntity.ok(results);
    }
}
