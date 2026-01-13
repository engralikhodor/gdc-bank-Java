package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.service.CustomerService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/")
@RequiredArgsConstructor
public class CustomerController
{
    private final CustomerService customerService;

    // Create a new customer
    @PostMapping
    public ResponseEntity<GlobalResponse<CustomerResponse>>
    createAccount(@Valid @RequestBody CustomerRequest request)
    {
        GlobalResponse<CustomerResponse> response = customerService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //@ModelAttribute used for mapping request => object
    @GetMapping("search")
    public ResponseEntity<GlobalResponse<List<CustomerResponse>>>
    searchCustomer(@ModelAttribute CustomerSearchCriteria criteria)
    {
        List<CustomerResponse> results = customerService.searchCustomers(criteria);
        boolean found = !results.isEmpty();

        GlobalResponse<List<CustomerResponse>> response = GlobalResponse.<List<CustomerResponse>>builder()
                .responseCode(found ? AccountUtils.CUSTOMER_FOUND_CODE : AccountUtils.CUSTOMER_NOT_FOUND_CODE)
                .responseMessage(found ? AccountUtils.CUSTOMER_FOUND : AccountUtils.CUSTOMER_NOT_FOUND)
                .data(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // Get balance of customer
    @GetMapping("balanceEnquiry")
    public ResponseEntity<GlobalResponse<CustomerResponse>>
    balanceEnquiry(@RequestBody EnquiryRequest enquiryRequest)
    {
        GlobalResponse<CustomerResponse> response = customerService.balanceEnquiry(enquiryRequest);

        HttpStatus status = response.responseCode().equals(AccountUtils.CUSTOMER_NOT_FOUND_CODE)
                ? HttpStatus.NOT_FOUND
                : HttpStatus.OK;

        return new ResponseEntity<>(response, status);
    }

    // Get name of customer
    @GetMapping("nameEnquiry")
    public ResponseEntity<GlobalResponse<String>>
    nameEnquiry(@RequestBody EnquiryRequest enquiryRequest)
    {
        GlobalResponse<String> response = customerService.nameEnquiry(enquiryRequest);

        HttpStatus status = response.responseCode().equals(AccountUtils.CUSTOMER_NOT_FOUND_CODE)
                ? HttpStatus.NOT_FOUND
                : HttpStatus.OK;

        return new ResponseEntity<>(response, status);
    }

    // Credit an account (by account number)
    @PostMapping("credit")
    public ResponseEntity<GlobalResponse<CustomerResponse>>
    creditAccount(@Valid @RequestBody CreditDebitRequest request)
    {
        GlobalResponse<CustomerResponse> response = customerService.creditAccount(request);
        HttpStatus status = response.responseCode().equals(AccountUtils.CUSTOMER_CREDITED_SUCCESSFULLY_CODE)
                ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    // Debit an account (by account number)
    @PostMapping("debit")
    public ResponseEntity<GlobalResponse<CustomerResponse>>
    debitAccount(@Valid @RequestBody CreditDebitRequest request)
    {
        GlobalResponse<CustomerResponse> response = customerService.debitAccount(request);
        HttpStatus status = response.responseCode().equals(AccountUtils.CUSTOMER_DEBITED_SUCCESSFULLY_CODE)
                ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    @PostMapping("transfer")
    public ResponseEntity<GlobalResponse<CustomerResponse>>
    transferAmount(@Valid @RequestBody TransferRequest transferRequest)
    {
        GlobalResponse<CustomerResponse> response = customerService.transferAmount(transferRequest);

        if (response.responseCode().equals(AccountUtils.AMOUNT_TRANSFERRED_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
