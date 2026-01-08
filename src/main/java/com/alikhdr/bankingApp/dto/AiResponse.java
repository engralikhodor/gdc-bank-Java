package com.alikhdr.bankingApp.dto;

public record AiResponse
        (
                String model,
                String role,
                String content,
                String prompt
        )
{
}
