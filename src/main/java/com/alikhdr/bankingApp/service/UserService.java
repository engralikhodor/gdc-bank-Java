package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.*;

import java.util.List;

public interface UserService
{
    String nameEnquiry(EnquiryRequest enquiryRequest);

    ApiResponse<UserResponse> createAccount(UserRequest userRequest);

    ApiResponse<UserResponse> balanceEnquiry(EnquiryRequest enquiryRequest);

    ApiResponse<UserResponse> creditAccount(CreditDebitRequest creditDebitRequest);

    ApiResponse<UserResponse> debitAccount(CreditDebitRequest creditDebitRequest);

    // Changed to UserResponse to provide the updated sender's balance
    ApiResponse<UserResponse> transferAmount(TransferRequest transferRequest);

    List<UserResponse> searchUsers(UserSearchCriteria userSearchCriteria);
}
