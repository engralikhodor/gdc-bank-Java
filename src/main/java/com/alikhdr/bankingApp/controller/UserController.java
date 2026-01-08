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
@RequestMapping("/api/user/")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    // Create a new user
    @PostMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>>
    createAccount(@Valid @RequestBody UserRequest userRequest)
    {
        ApiResponse<List<UserResponse>> response = userService.createAccount(userRequest);
        if (response.responseCode().equals(AccountUtils.ACCOUNT_EXISTS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get balance of user
    @GetMapping("balanceEnquiry")
    public ResponseEntity<ApiResponse<List<UserResponse>>>
    balanceEnquiry(@RequestBody EnquiryRequest enquiryRequest)
    {
        ApiResponse<List<UserResponse>> response = userService.balanceEnquiry(enquiryRequest);
        if (response.responseCode().equals(AccountUtils.ACCOUNT_NOT_FOUND_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get name of user
    @GetMapping("nameEnquiry")
    public ResponseEntity<ApiResponse>
    nameEnquiry(@RequestBody EnquiryRequest enquiryRequest)
    {
        String name = userService.nameEnquiry(enquiryRequest);
        if (name.isEmpty())
        {
            ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .data(null)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                //todo check
                //                .userResponseDTO(UserResponseDTO.builder().accountName(name).build())
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Credit an account (by account number)
    @PostMapping("credit")
    public ResponseEntity<ApiResponse<List<UserResponse>>>
    creditAccount(@Valid @RequestBody CreditDebitRequest creditDebitRequest)
    {
        ApiResponse<List<UserResponse>> response = userService.creditAccount(creditDebitRequest);
        if (response.responseCode().equals(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Debit an account (by account number)
    @PostMapping("debit")
    public ResponseEntity<ApiResponse<List<UserResponse>>>
    debitAccount(@Valid @RequestBody CreditDebitRequest creditDebitRequest)
    {
        ApiResponse<List<UserResponse>> response = userService.debitAccount(creditDebitRequest);
        if (response.responseCode().equals(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("transfer")
    public ResponseEntity<ApiResponse<List<UserResponse>>>
    transferAmount(@Valid @RequestBody TransferRequest transferRequest)
    {
        ApiResponse<List<UserResponse>> response = userService.transferAmount(transferRequest);
        if (response.responseCode().equals(AccountUtils.TRANSFER_SUCCESS_CODE))
        {
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>>
    searchUser(@ModelAttribute UserSearchCriteria userSearchCriteria)
    //@ModelAttribute used for mapping request => object
    {
        List<UserResponse> results = userService.searchUsers(userSearchCriteria);
        boolean found = !results.isEmpty();

        ApiResponse<List<UserResponse>> response =
                ApiResponse.<List<UserResponse>>builder()
                        .responseCode(found ? AccountUtils.USER_FOUND_SUCCESS_CODE
                                : AccountUtils.USER_NOT_FOUND_SUCCESS_CODE
                        )
                        .responseMessage(
                                found ? AccountUtils.USER_FOUND_SUCCESS_MESSAGE
                                        : AccountUtils.USER_NOT_FOUND_SUCCESS_MESSAGE
                        )
                        .data(results)
                        .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
