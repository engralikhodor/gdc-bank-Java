package com.alikhdr.bankingApp.dto;

import lombok.Builder;

// Generic response
@Builder
public record ApiResponse<T>(
        String responseCode,
        String responseMessage,
        T data
)
{
}
