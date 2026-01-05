package com.alikhdr.bankingApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class OpenAiWebClientConfig
{

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.base-url}")
    private String baseUrl;

    @Value("${ai.openai.timeout-ms}")
    private long timeoutMs;

    @Bean
    public WebClient openAiWebClient(@Value("${ai.openai.api-key}") String apiKey)
    {
        {
            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .clientConnector(
                            new org.springframework.http.client.reactive.ReactorClientHttpConnector(
                                    reactor.netty.http.client.HttpClient.create()
                                            .responseTimeout(Duration.ofMillis(timeoutMs))
                            )
                    )
                    .build();
        }
    }
}
