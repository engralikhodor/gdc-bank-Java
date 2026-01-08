package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.*;

import java.util.List;

public interface UserService
{
    String nameEnquiry(EnquiryRequest enquiryRequest);

    ApiResponse createAccount(UserRequest userRequest);

    ApiResponse balanceEnquiry(EnquiryRequest enquiryRequest);

    ApiResponse creditAccount(CreditDebitRequest creditDebitRequest);

    ApiResponse debitAccount(CreditDebitRequest creditDebitRequest);

    ApiResponse transferAmount(TransferRequest transferRequest);

    List<UserResponse> searchUsers(UserSearchCriteria userSearchCriteria);
}
