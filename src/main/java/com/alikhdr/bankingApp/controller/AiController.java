package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.AiResponse;
import com.alikhdr.bankingApp.service.ai.AiTransactionInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/transactions")
@RequiredArgsConstructor
public class AiController
{

    private final AiTransactionInsightService aiService;

    /**
     * Generate AI insights for all transactions of a given account number.
     */
    @PostMapping("/generate")
    public ResponseEntity<AiResponse> generateInsights(@RequestBody AiAccountRequest request)
    {

        AiResponse aiResponse = aiService.generateInsightsForAccount(request.getAccountNumber());

        return ResponseEntity.ok(aiResponse);
    }


    // Request DTO
    public static class AiAccountRequest
    {
        private String accountNumber;

        public String getAccountNumber()
        {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber)
        {
            this.accountNumber = accountNumber;
        }
    }
}
