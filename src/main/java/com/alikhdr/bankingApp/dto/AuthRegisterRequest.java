package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.RoleOptions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * username, password, role, customerRequest
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegisterRequest
{
    @NotBlank(message = "Username is required.")
    @Size(min = 5, max = 50, message = "Username should be between 5 and 50 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 7, message = "Password should be more than 6 characters.")
    private String password;

    @NotNull(message = "Role is required.")
    private RoleOptions role;

    @Valid
    @NotNull(message = "Customer details are required.")
    private CustomerRequest customerRequest;
}
