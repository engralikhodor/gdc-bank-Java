package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.*;

import java.util.List;

public interface CustomerService
{
    GlobalResponse<CustomerResponse> createAccount(CustomerRequest customerRequest);

    GlobalResponse<CustomerResponse> balanceEnquiry(EnquiryRequest enquiryRequest);

    GlobalResponse<CustomerResponse> creditAccount(CreditDebitRequest creditDebitRequest);

    GlobalResponse<CustomerResponse> debitAccount(CreditDebitRequest creditDebitRequest);

    GlobalResponse<CustomerResponse> transferAmount(TransferRequest transferRequest);

    List<CustomerResponse> searchCustomers(CustomerSearchCriteria customerSearchCriteria);

    GlobalResponse<String> nameEnquiry(EnquiryRequest enquiryRequest);
}
