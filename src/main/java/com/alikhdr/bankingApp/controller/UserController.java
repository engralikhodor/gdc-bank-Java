package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.service.UserService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    // Create a new user
    @PostMapping
    public ResponseEntity<ResponseDTO>
    createAccount(@Valid @RequestBody UserRequestDTO userRequestDTO)
    {
        ResponseDTO response = userService.createAccount(userRequestDTO);
        if (response.responseCode().equals(AccountUtils.ACCOUNT_EXISTS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get balance of user
    @GetMapping("balanceEnquiry")
    public ResponseEntity<ResponseDTO>
    balanceEnquiry(@RequestBody EnquiryRequestDTO enquiryRequestDTO)
    {
        ResponseDTO response = userService.balanceEnquiry(enquiryRequestDTO);
        if (response.responseCode().equals(AccountUtils.ACCOUNT_NOT_FOUND_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get name of user
    @GetMapping("nameEnquiry")
    public ResponseEntity<ResponseDTO>
    nameEnquiry(@RequestBody EnquiryRequestDTO enquiryRequestDTO)
    {
        String name = userService.nameEnquiry(enquiryRequestDTO);
        if (name.isEmpty())
        {
            ResponseDTO response = ResponseDTO.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .accountInfo(null)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseDTO response = ResponseDTO.builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .accountInfo(AccountInfoDTO.builder()
                        .accountName(name)
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Credit an account (by account number)
    @PostMapping("credit")
    public ResponseEntity<ResponseDTO>
    creditAccount(@Valid @RequestBody CreditDebitRequestDTO creditDebitRequestDTO)
    {
        ResponseDTO response = userService.creditAccount(creditDebitRequestDTO);
        if (response.responseCode().equals(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Debit an account (by account number)
    @PostMapping("debit")
    public ResponseEntity<ResponseDTO>
    debitAccount(@Valid @RequestBody CreditDebitRequestDTO creditDebitRequestDTO)
    {
        ResponseDTO response = userService.debitAccount(creditDebitRequestDTO);
        if (response.responseCode().equals(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("transfer")
    public ResponseEntity<ResponseDTO>
    transferAmount(@Valid @RequestBody TransferRequestDTO transferRequestDTO)
    {
        ResponseDTO response = userService.transferAmount(transferRequestDTO);
        if (response.responseCode().equals(AccountUtils.TRANSFER_SUCCESS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
