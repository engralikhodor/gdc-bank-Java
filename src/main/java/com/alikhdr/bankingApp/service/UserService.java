package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.*;

public interface UserService
{
    String nameEnquiry(EnquiryRequestDTO enquiryRequestDTO);

    ResponseDTO createAccount(UserRequestDTO userRequestDTO);

    ResponseDTO balanceEnquiry(EnquiryRequestDTO enquiryRequestDTO);

    ResponseDTO creditAccount(CreditDebitRequestDTO creditDebitRequestDTO);

    ResponseDTO debitAccount(CreditDebitRequestDTO creditDebitRequestDTO);

    ResponseDTO transferAmount(TransferRequestDTO transferRequestDTO);
}
