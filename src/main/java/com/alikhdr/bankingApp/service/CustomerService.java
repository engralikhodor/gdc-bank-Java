package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.*;

import java.util.List;

public interface CustomerService
{
    CustomerResponse openInitialAccount(CustomerRequest customerRequest);

    GlobalResponse<CustomerResponse> balanceEnquiry(EnquiryRequest enquiryRequest);

    GlobalResponse<CustomerResponse> creditAccount(CreditDebitRequest creditDebitRequest);

    GlobalResponse<CustomerResponse> debitAccount(CreditDebitRequest creditDebitRequest);

    GlobalResponse<CustomerResponse> transferAmount(TransferRequest transferRequest);

    GlobalResponse<List<CustomerResponse>> searchCustomers(CustomerSearchCriteria customerSearchCriteria);

    GlobalResponse<String> nameEnquiry(EnquiryRequest enquiryRequest);
}
