package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController
{
    private final CustomerService customerService;

    @GetMapping("/enquiry/name")
    public ResponseEntity<GlobalResponse<String>> nameEnquiry(@Valid @ModelAttribute EnquiryRequest request)
    {
        return ResponseEntity.ok(customerService.nameEnquiry(request));
    }

    @GetMapping("/enquiry/balance")
    public ResponseEntity<GlobalResponse<CustomerResponse>> balanceEnquiry(@Valid @ModelAttribute EnquiryRequest request)
    {
        return ResponseEntity.ok(customerService.balanceEnquiry(request));
    }

    @PostMapping("/credit")
    public ResponseEntity<GlobalResponse<CustomerResponse>> credit(@Valid @RequestBody CreditDebitRequest request)
    {
        return ResponseEntity.ok(customerService.creditAccount(request));
    }

    @PostMapping("/debit")
    public ResponseEntity<GlobalResponse<CustomerResponse>> debit(@Valid @RequestBody CreditDebitRequest request)
    {
        return ResponseEntity.ok(customerService.debitAccount(request));
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalResponse<List<CustomerResponse>>> search(@Valid @ModelAttribute CustomerSearchCriteria criteria)
    {
        return ResponseEntity.ok(customerService.searchCustomers(criteria));
    }
}
