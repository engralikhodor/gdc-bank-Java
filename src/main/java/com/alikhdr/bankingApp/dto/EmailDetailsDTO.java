package com.alikhdr.bankingApp.dto;

import lombok.Builder;

@Builder
public record EmailDetailsDTO(
        String recipient,
        String messageBody,
        String subject,
        String attachment
)
{
}
