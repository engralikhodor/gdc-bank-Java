package com.alikhdr.bankingApp.dto;

import lombok.Builder;

@Builder
public record ResponseDTO(
        String responseCode,
        String responseMessage,
        AccountInfoDTO accountInfo
)
{
}
