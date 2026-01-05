package com.alikhdr.bankingApp.service.ai;

import com.alikhdr.bankingApp.dto.AiResponseDTO;
import com.alikhdr.bankingApp.entity.Transaction;
import com.alikhdr.bankingApp.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiTransactionInsightService
{

    private final WebClient openAiWebClient;
    private final TransactionRepository transactionRepository;

    @Value("${ai.openai.model}")
    private String model;

    @Value("${ai.openai.max-tokens}")
    private int maxTokens;

    /**
     * Generate AI insights for all transactions of a given account.
     * Returns a clean DTO with the AI response and the prompt.
     */
    public AiResponseDTO generateInsightsForAccount(String accountNumber)
    {

        // Fetch transactions from DB
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);

        if (transactions.isEmpty())
        {
            String promptEmpty = "No transactions found for account " + maskAccount(accountNumber);
            return new AiResponseDTO(model, "assistant", "No transactions found for this account.", promptEmpty);
        }

        // Build safe prompt
        String prompt = buildPrompt(transactions);

        // Call OpenAI
        String rawResponse = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", new Object[]{
                                Map.of("role", "system", "content", "You are a banking assistant. Explain transactions clearly and concisely."),
                                Map.of("role", "user", "content", prompt)
                        },
                        "max_tokens", maxTokens
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse only the fields we want
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            JsonNode choice = root.path("choices").get(0);
            String role = choice.path("message").path("role").asText();
            String content = choice.path("message").path("content").asText();

            return new AiResponseDTO(root.path("model").asText(), role, prompt, content);
        }
        catch (Exception e)
        {
            return new AiResponseDTO(model, "assistant", "Failed to parse AI response.", prompt);
        }
    }

    /**
     * Build a masked, human-readable summary of transactions for AI.
     */
    private String buildPrompt(List<Transaction> transactions)
    {
        String summary = transactions.stream()
                .map(t -> "Type=" + t.getTransactionType()
                        + ", Amount=" + t.getAmount()
                        + ", Status=" + t.getStatus()
                        + ", Date=" + t.getCreatedAt())
                .collect(Collectors.joining("\n"));

        return "You are a banking assistant. Summarize the following transactions for the user in plain language, " +
                "group credits and debits, and explain the overall status. Do not repeat the raw transaction list:\n" +
                summary;
    }

    /**
     * Mask account number for safety
     */
    private String maskAccount(String accountNumber)
    {
        if (accountNumber == null || accountNumber.length() < 4)
            return "****";
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
