package com.alikhdr.bankingApp.dto;

public record AiResponseDTO(
        String model,
        String role,
        String prompt,
        String content
)
{
}
