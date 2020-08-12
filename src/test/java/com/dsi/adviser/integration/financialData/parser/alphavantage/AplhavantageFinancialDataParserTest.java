package com.dsi.adviser.integration.financialData.parser.alphavantage;

import com.dsi.adviser.integration.financialData.parser.RawFinancialData;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

class AplhavantageFinancialDataParserTest {

    private final String PAYLOAD;
    private final AplhavantageFinancialDataParser parser = new AplhavantageFinancialDataParser(Jackson2ObjectMapperBuilder.json()
            .failOnUnknownProperties(false)
            .build());

    @SneakyThrows
    public AplhavantageFinancialDataParserTest() {
        PAYLOAD = new String(Files.readAllBytes(Paths.get(getClass().getResource("response-example.json").toURI())));
    }

    @Test
    void parsePayload() {
        RawFinancialData result = parser.parse(PAYLOAD);
        assertEquals(LocalDate.of(2020,6,30), result.getDate());
        assertEquals(15576999936L * 11.18, result.getEnterpriseValue(), 0.0001);
        assertEquals(15576999936L, result.getEarnings(), 0.0001);
        assertEquals(0.0522, result.getDividendsApy(), 0.0001);
        assertEquals(90.56, result.getPriceMinYtd(), 0.0001);
        assertEquals(158.75, result.getPriceMaxYtd(), 0.0001);
    }
}