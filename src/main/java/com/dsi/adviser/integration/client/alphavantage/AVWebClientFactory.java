package com.dsi.adviser.integration.client.alphavantage;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
@ConditionalOnBean({AlphaVantageProperties.class})
public class AVWebClientFactory {
    private static final String API_KEY_PARAM = "apikey";
    public static final int BUFFER_SIZE = 20 * 1024 * 1024;
    private final AlphaVantageProperties properties;

    public WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(properties.getUrl())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codec -> codec.defaultCodecs().maxInMemorySize(BUFFER_SIZE))
                        .build())
                .defaultUriVariables(getDefaultUriVariables())
                .build();
    }

    private HashMap<String, Object> getDefaultUriVariables() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(API_KEY_PARAM, properties.getApiKey());
        return map;
    }
}