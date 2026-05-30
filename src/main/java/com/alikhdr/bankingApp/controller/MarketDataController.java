package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.dto.FinnhubQuoteDto;
import com.alikhdr.bankingApp.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class MarketDataController
{

    private final MarketDataService marketDataService;

    /**
     * Retrieves the current stock quote for a given symbol.
     *
     * @param symbol The stock ticker symbol (e.g., "AAPL").
     * @return A Mono emitting ResponseEntity containing FinnhubQuoteDto or a 404 if not found.
     */
    @GetMapping("/quote")
    public Mono<ResponseEntity<FinnhubQuoteDto>> getStockQuote(@RequestParam String symbol)
    {
        return marketDataService.getStockQuote(symbol)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
