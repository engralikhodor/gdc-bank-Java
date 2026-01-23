package com.alikhdr.bankingApp.service.ai;

import com.alikhdr.bankingApp.dto.AiResponse;
import com.alikhdr.bankingApp.entity.Transaction;
import com.alikhdr.bankingApp.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiTransactionInsightService
{
    private final WebClient openAiWebClient;
    private final TransactionRepository transactionRepository;

    @Value("${ai.openai.model}")
    private String model;

    @Value("${ai.openai.max-tokens}")
    private int maxTokens;

    // generate AI insights for all transactions of a given account: return DTO with AI response & prompt
    // name = openaiService => same in app.yml
    @CircuitBreaker(name = "openaiService", fallbackMethod = "fallbackGenerateInsights")
    public AiResponse generateInsightsForAccount(String accountNumber)
    {
        // Fetch transactions from DB
        List<Transaction> transactions = transactionRepository.findByDestinationAccountNumber(accountNumber);

        if (transactions.isEmpty())
        {
            String promptEmpty = "No transactions found for account " + maskAccount(accountNumber);
            return new AiResponse(model, "assistant", "No transactions found for this account.", promptEmpty);
        }

        String prompt = buildPrompt(transactions);

        String rawResponse = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", new Object[]{
                                Map.of(
                                        "role",
                                        "system",
                                        "content",
                                        "You are a banking assistant. Explain transactions clearly and concisely."),
                                Map.of(
                                        "role",
                                        "user",
                                        "content",
                                        prompt)
                        },
                        "max_tokens", maxTokens
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // parse specific fields
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            JsonNode choice = root.path("choices").get(0);
            String role = choice.path("message").path("role").asText();
            String content = choice.path("message").path("content").asText();

            return new AiResponse(root.path("model").asText(), role, content, prompt);
        }
        catch (Exception e)
        {
            return new AiResponse(model, "assistant", "Failed to parse AI response.", prompt);
        }
    }

    // FALLBACK METHOD, called when: OpenAI returns an error (408, 500...)  - or - if circuit is "OPEN" (already failed too many times)
    public AiResponse fallbackGenerateInsights(String accountNumber, Throwable t)
    {
        log.error("AI Insights failed for account {}. Reason: {}", accountNumber, t.getMessage());

        return new AiResponse(
                model,
                "assistant",
                "Notice: Financial insights are temporarily unavailable due to external service latency. Please try again in a few minutes.",
                "Your transactions are safe, but our AI advisor is currently unreachable."
        );
    }

    //  build a masked, human-readable summary of transactions for AI
    private String buildPrompt(List<Transaction> transactions)
    {
        String summary = transactions.stream()
                .map(t -> t.getTransactionType() + ": $" + t.getAmount())
                .collect(Collectors.joining(", "));

        return "You are a professional banking assistant. Analyze these transactions: " + summary + "\n\n" +
                "STRICT RULES:\n" +
                "1. DO NOT list individual transactions or numbers.\n" +
                "2. Group everything into three short paragraphs: Credits, Debits, and Overall Financial Health.\n" +
                "3. Use professional currency formatting (e.g. $1,234.56).\n" +
                "4. Be concise and human-friendly.";
    }

    // mask account number for safety
    private String maskAccount(String accountNumber)
    {
        if (accountNumber == null || accountNumber.length() < 4)
            return "****";
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    public Flux<String> getStreamingInsights(String accountNumber)
    {
        List<Transaction> transactions = transactionRepository.findByDestinationAccountNumber(accountNumber);
        String prompt = buildPrompt(transactions);

        return openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                        "model", "gpt-3.5-turbo",
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "stream", true
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .map(chunk ->
                {
                    if (chunk.contains("[DONE]"))
                        return "";
                    try
                    {
                        ObjectMapper mapper = new ObjectMapper();
                        // OpenAI sends "data: {...}". We need the JSON part.
                        String json = chunk.replace("data:", "").trim();
                        JsonNode node = mapper.readTree(json);

                        // Extract just the text content
                        JsonNode contentNode = node.path("choices").get(0).path("delta").path("content");
                        return contentNode.isMissingNode() ? "" : contentNode.asText();
                    }
                    catch (Exception e)
                    {
                        return ""; // Skip malformed chunks
                    }
                })
                .filter(text -> !text.isEmpty());
    }
}
