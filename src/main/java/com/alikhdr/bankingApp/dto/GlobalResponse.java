package com.alikhdr.bankingApp.dto;

import lombok.Builder;

// Generic response
@Builder
public record GlobalResponse<T>(
        String responseCode,
        String responseMessage,
        T data
)
{
}
