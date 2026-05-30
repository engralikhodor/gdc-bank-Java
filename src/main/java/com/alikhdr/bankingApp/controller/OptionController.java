// REST controller for managing financial options and calculating their theoretical prices.
// Provides endpoints for creating, retrieving, and pricing Option entities.
package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.entity.Option;
import com.alikhdr.bankingApp.exception.ResourceNotFoundException; // Import the custom exception
import com.alikhdr.bankingApp.repository.OptionRepository;
import com.alikhdr.bankingApp.service.OptionPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
public class OptionController {

    private final OptionRepository optionRepository;
    private final OptionPricingService optionPricingService;

    /**
     * Creates a new financial option.
     * @param option The Option entity to create.
     * @return A ResponseEntity containing the created Option and HTTP status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Option> createOption(@RequestBody Option option) {
        Option savedOption = optionRepository.save(option);
        return new ResponseEntity<>(savedOption, HttpStatus.CREATED);
    }

    /**
     * Retrieves a financial option by its ID.
     * @param id The ID of the option to retrieve.
     * @return A ResponseEntity containing the Option if found, or HTTP status 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Option> getOptionById(@PathVariable Long id) {
        return optionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Calculates the theoretical price of an existing financial option.
     * @param id The ID of the option for which to calculate the price.
     * @return A Mono emitting ResponseEntity containing the calculated price as BigDecimal,
     *         or HTTP status 404 if the option is not found, or 500 if an error occurs during pricing.
     */
    @GetMapping("/{id}/price")
    public Mono<ResponseEntity<BigDecimal>> getOptionPrice(@PathVariable Long id) {
        return Mono.justOrEmpty(optionRepository.findById(id))
                // Use ResourceNotFoundException for clarity
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Option not found with ID: " + id)))
                .flatMap(optionPricingService::calculateOptionPrice)
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, e -> {
                    // Handle ResourceNotFoundException specifically
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .onErrorResume(e -> {
                    // Catch any other unexpected errors during pricing
                    // In a real application, log the error for debugging
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
