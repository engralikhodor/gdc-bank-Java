package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.AiResponse;
import com.alikhdr.bankingApp.service.ai.AiTransactionInsightService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai/transactions")
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

    @PostMapping(value = "/insights/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public Flux<String> streamInsights(@RequestBody AiAccountRequest request)
    {
        return aiService.getStreamingInsights(request.getAccountNumber());
    }

    @Data
    @NoArgsConstructor // Required for JSON parsing
    @AllArgsConstructor
    public static class AiAccountRequest
    {
        private String accountNumber;

        // Manual getter as a safety backup for Spring
        public String getAccountNumber()
        {
            return accountNumber;
        }
    }
}
