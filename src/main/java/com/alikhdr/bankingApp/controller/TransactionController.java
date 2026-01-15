package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.GlobalResponse;
import com.alikhdr.bankingApp.dto.TransactionResponse;
import com.alikhdr.bankingApp.dto.TransactionSearchCriteria;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController
{
    private final TransactionService transactionService;

    @GetMapping("/search")
    public ResponseEntity<GlobalResponse<List<TransactionResponse>>>
    searchTransactions(@ModelAttribute TransactionSearchCriteria searchCriteria)

    //@ModelAttribute used for mapping request => object
    {
        List<TransactionResponse> results = transactionService.searchTransactions(searchCriteria);
        boolean found = !results.isEmpty();

        GlobalResponse<List<TransactionResponse>> response =
                GlobalResponse.<List<TransactionResponse>>builder()
                        .responseCode(found ? AccountUtils.TRANSACTION_FOUND_SUCCESSFULLY_CODE
                                : AccountUtils.TRANSACTION_NOT_FOUND_CODE
                        )
                        .responseMessage(
                                found ? AccountUtils.TRANSACTION_FOUND_SUCCESSFULLY
                                        : AccountUtils.TRANSACTION_NOT_FOUND
                        )
                        .data(results)
                        .build();

        return new ResponseEntity<>(response, HttpStatus.OK);

    }
}
