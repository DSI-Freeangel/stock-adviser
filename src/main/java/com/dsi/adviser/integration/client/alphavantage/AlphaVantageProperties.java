package com.dsi.adviser.integration.client.alphavantage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Validated
@Component
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("app.integration.alphavantage")
@ConditionalOnProperty(prefix = "app.integration.alphavantage", name = "enabled")
public class AlphaVantageProperties {
    @NotBlank
    private String url;
    @NotEmpty
    private List<String> apiKeys;
}