// Service for calculating the theoretical price of financial options using the Black-Scholes model.
package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.entity.Option;
import com.alikhdr.bankingApp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class OptionPricingService
{

    private final MarketDataService marketDataService;

    @Value("${option.pricing.risk-free-rate}")
    private double riskFreeRate;
    @Value("${option.pricing.volatility}")
    private double volatility;

    /**
     * Approximates the cumulative distribution function (CDF) of the standard normal distribution.
     * This is a common approximation used in Black-Scholes.
     *
     * @param x The value for which to calculate the CDF.
     * @return The CDF value.
     */
    private double cdf(double x)
    {
        double L = Math.abs(x);
        double K = 1.0 / (1.0 + 0.2316419 * L);
        double cdf = 1.0 - 1.0 / Math.sqrt(2 * Math.PI) * Math.exp(-L * L / 2.0) *
                (0.319381530 * K + -0.356563782 * K * K + 1.781477937 * K * K * K +
                        -1.821255978 * K * K * K * K + 1.330274429 * K * K * K * K * K);
        if (x < 0)
        {
            cdf = 1.0 - cdf;
        }
        return cdf;
    }

    /**
     * Calculates the theoretical price of a European option using the Black-Scholes model.
     * Fetches the current underlying asset price from the MarketDataService.
     *
     * @param option The Option entity containing details like symbol, type, strike price, and expiration date.
     * @return A Mono emitting the calculated option price as BigDecimal.
     */
    public Mono<BigDecimal> calculateOptionPrice(Option option)
    {
        return marketDataService.getStockQuote(option.getSymbol())
                .flatMap(quote ->
                {
                    if (quote == null || quote.getC() == null)
                    {
                        return Mono.error(new ResourceNotFoundException("Could not retrieve current price for symbol: " + option.getSymbol()));
                    }

                    BigDecimal underlyingPrice = quote.getC();
                    BigDecimal strikePrice = option.getStrikePrice();

                    long daysToExpiration = ChronoUnit.DAYS.between(LocalDate.now(), option.getExpirationDate());
                    if (daysToExpiration <= 0)
                    {
                        // Option has expired or expires today, price is intrinsic value or zero
                        if (option.getType() == Option.OptionType.CALL)
                        {
                            return Mono.just(underlyingPrice.subtract(strikePrice).max(BigDecimal.ZERO));
                        }
                        else
                        { // PUT
                            return Mono.just(strikePrice.subtract(underlyingPrice).max(BigDecimal.ZERO));
                        }
                    }
                    double timeToExpiration = daysToExpiration / 365.0; // Convert days to years

                    // Black-Scholes formula components
                    double d1 = (Math.log(underlyingPrice.doubleValue() / strikePrice.doubleValue()) +
                            (riskFreeRate + (volatility * volatility) / 2.0) * timeToExpiration) /
                            (volatility * Math.sqrt(timeToExpiration));

                    double d2 = d1 - volatility * Math.sqrt(timeToExpiration);

                    double optionPrice;
                    if (option.getType() == Option.OptionType.CALL)
                    {
                        optionPrice = underlyingPrice.doubleValue() * cdf(d1) -
                                strikePrice.doubleValue() * Math.exp(-riskFreeRate * timeToExpiration) * cdf(d2);
                    }
                    else
                    { // PUT
                        optionPrice = strikePrice.doubleValue() * Math.exp(-riskFreeRate * timeToExpiration) * cdf(-d2) -
                                underlyingPrice.doubleValue() * cdf(-d1);
                    }

                    return Mono.just(BigDecimal.valueOf(optionPrice).setScale(2, RoundingMode.HALF_UP));
                });
    }
}
