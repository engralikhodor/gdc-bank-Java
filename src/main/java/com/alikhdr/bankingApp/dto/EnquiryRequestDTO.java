package com.alikhdr.bankingApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnquiryRequestDTO
{
    @NotBlank(message = "The account number can't be empty.")
    private String accountNumber;
}
