package com.dsi.adviser.integration.client.alphavantage;

public class FrequencyLimitException extends RuntimeException {
    public FrequencyLimitException(String message) {
        super(message);
    }
}
