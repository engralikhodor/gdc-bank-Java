package com.alikhdr.bankingApp.dto;

import com.alikhdr.bankingApp.entity.RoleOptions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class AuthRequest
{
    @NotBlank(message = "Username is required.")
    @Size(min = 5, max = 50, message = "Username should be between 5 and 50 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 7, message = "Password should be more than 6 characters.")
    private String password;

    @NotNull(message = "Role is required.")
    private RoleOptions role;

    @NotNull(message = "Customer ID is required.")
    private UUID customer_id;
}
