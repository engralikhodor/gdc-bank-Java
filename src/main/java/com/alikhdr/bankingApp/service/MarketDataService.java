package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.FinnhubQuoteDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MarketDataService
{

    private final WebClient webClient;
    @Value("${finnhub.api.key}")
    private String finnhubApiKey;

    public MarketDataService(WebClient.Builder webClientBuilder)
    {
        this.webClient = webClientBuilder.baseUrl("https://finnhub.io/api/v1").build();// non-blocking HTTP requests
    }

    /**
     * Fetches the current stock quote for a given symbol from Finnhub.
     *
     * @param symbol The stock ticker symbol (for ex: "AAPL").
     * @return A Mono emitting FinnhubQuoteDto containing the stock quote data.
     */
    public Mono<FinnhubQuoteDto> getStockQuote(String symbol)
    {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/quote")
                        .queryParam("symbol", symbol)
                        .queryParam("token", finnhubApiKey)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new RuntimeException("Failed to fetch market data: " + response.statusCode()))
                )
                .bodyToMono(FinnhubQuoteDto.class)
                .onErrorResume(e -> Mono.empty()); // request fails
    }
}
