package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.service.CustomerService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController
{

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalResponse<CustomerResponse> openInitialAccount(@Valid @RequestBody CustomerRequest request)
    {
        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(String.valueOf(HttpStatus.CREATED.value()))
                .responseMessage(AccountUtils.CUSTOMER_CREATED_SUCCESSFULLY)
                .data(customerService.openInitialAccount(request))
                .build();
    }

    @GetMapping("/nameEnquiry")
    public GlobalResponse<String> nameEnquiry(@RequestBody EnquiryRequest enquiryRequest)
    {
        return customerService.nameEnquiry(enquiryRequest);
    }

    @PostMapping("/balanceEnquiry")
    public GlobalResponse<CustomerResponse> balanceEnquiry(@RequestBody EnquiryRequest request)
    {
        return customerService.balanceEnquiry(request);
    }

    @PostMapping("/credit")
    public GlobalResponse<CustomerResponse> creditAccount(@Valid @RequestBody CreditDebitRequest request)
    {
        return customerService.creditAccount(request);
    }

    @PostMapping("/debit")
    public GlobalResponse<CustomerResponse> debitAccount(@Valid @RequestBody CreditDebitRequest request)
    {
        return customerService.debitAccount(request);
    }

    @GetMapping("/search")
    public GlobalResponse<List<CustomerResponse>> searchCustomer(@ModelAttribute CustomerSearchCriteria criteria)
    {
        return customerService.searchCustomers(criteria);
    }
}
