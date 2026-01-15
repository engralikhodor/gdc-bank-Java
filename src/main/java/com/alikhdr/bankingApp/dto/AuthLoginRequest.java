package com.alikhdr.bankingApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthLoginRequest
{

    @NotBlank(message = "Username is required.")
    private String username;

    @NotBlank(message = "Password is required.")
    private String password;
}
