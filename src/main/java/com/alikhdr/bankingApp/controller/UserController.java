package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.service.UserService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    // Create a new user
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>>
    createAccount(@Valid @RequestBody UserRequest request)
    {
        ApiResponse<UserResponse> response = userService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    //@ModelAttribute used for mapping request => object
    @GetMapping("search")
    public ResponseEntity<ApiResponse<List<UserResponse>>>
    searchUser(@ModelAttribute UserSearchCriteria criteria)
    {
        List<UserResponse> results = userService.searchUsers(criteria);
        boolean found = !results.isEmpty();

        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .responseCode(found ? AccountUtils.USER_FOUND_SUCCESS_CODE : AccountUtils.USER_NOT_FOUND_SUCCESS_CODE)
                .responseMessage(found ? AccountUtils.USER_FOUND_SUCCESS_MESSAGE : AccountUtils.USER_NOT_FOUND_SUCCESS_MESSAGE)
                .data(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // Get balance of user
    @GetMapping("balanceEnquiry")
    public ResponseEntity<ApiResponse<UserResponse>>
    balanceEnquiry(@RequestBody EnquiryRequest enquiryRequest)
    {
        // Services now return ApiResponse<UserResponse> instead of List
        ApiResponse<UserResponse> response = userService.balanceEnquiry(enquiryRequest);

        HttpStatus status = response.responseCode().equals(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                ? HttpStatus.NOT_FOUND
                : HttpStatus.OK;

        return new ResponseEntity<>(response, status);
    }

    // Get name of user
    @GetMapping("nameEnquiry")
    public ResponseEntity<ApiResponse<String>>
    nameEnquiry(@RequestBody EnquiryRequest enquiryRequest)
    {
        String name = userService.nameEnquiry(enquiryRequest);

        // If name equals the error message constant, return BAD_REQUEST
        if (name.equals(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE))
        {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .data(null)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        ApiResponse<String> response = ApiResponse.<String>builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .data(name) // Pass the actual name string to the generic data field
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Credit an account (by account number)
    @PostMapping("credit")
    public ResponseEntity<ApiResponse<UserResponse>>
    creditAccount(@Valid @RequestBody CreditDebitRequest request)
    {
        ApiResponse<UserResponse> response = userService.creditAccount(request);
        HttpStatus status = response.responseCode().equals(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE)
                ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    // Debit an account (by account number)
    @PostMapping("debit")
    public ResponseEntity<ApiResponse<UserResponse>>
    debitAccount(@Valid @RequestBody CreditDebitRequest request)
    {
        ApiResponse<UserResponse> response = userService.debitAccount(request);
        HttpStatus status = response.responseCode().equals(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE)
                ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    @PostMapping("transfer")
    public ResponseEntity<ApiResponse<UserResponse>>
    transferAmount(@Valid @RequestBody TransferRequest transferRequest)
    {
        ApiResponse<UserResponse> response = userService.transferAmount(transferRequest);

        if (response.responseCode().equals(AccountUtils.TRANSFER_SUCCESS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
