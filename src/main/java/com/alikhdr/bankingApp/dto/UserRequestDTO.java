//format of the request that we will receive to add a new user
package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.GenderOptions;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO
{
    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    private String otherName;

    @NotNull(message = "Gender is mandatory")
    private GenderOptions gender;

    @NotBlank(message = "Nationality is mandatory")
    private String nationality;

    private String address;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email address is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{8,15}$", message = "Phone number must be between 8 and 15 digits")
    private String phoneNumber;
    
    @Pattern(regexp = "^\\d{8,15}$", message = "Phone number must be between 8 and 15 digits")
    private String alternativePhoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Government ID is required")
    @Size(min = 9, max = 20, message = "Invalid Government ID length")
    private String governmentId;

    @NotBlank(message = "Occupation is required")
    private String occupation;

    // accountBalance, accountNumber, dailyTransferLimit, baseCurrency and status fields not to be set here
    // they will be set in UserServiceImpl
}

