package com.alikhdr.bankingApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
// (Filter): Used for Searching. It contains optional fields like minAge / not for creation
public class UserSearchCriteria
{
    @Positive(message = "Account number should be greater than zero.")
    private String accountNumber;

    @Email(message = "Invalid email address.")
    private String email;

    @Positive(message = "Minimum age should be greater than zero.")
    private Integer minAge;
}
