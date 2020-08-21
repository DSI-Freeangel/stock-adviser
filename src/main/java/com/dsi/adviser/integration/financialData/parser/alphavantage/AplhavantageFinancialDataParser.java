package com.dsi.adviser.integration.financialData.parser.alphavantage;

import com.dsi.adviser.integration.financialData.Source;
import com.dsi.adviser.integration.financialData.parser.FinancialDataParser;
import com.dsi.adviser.integration.financialData.parser.RawFinancialData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AplhavantageFinancialDataParser implements FinancialDataParser {
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public RawFinancialData parse(String json) {
        AlphavantageFinancialData avFinancialData = objectMapper
                .readValue(json, AlphavantageFinancialData.class);
        if(avFinancialData.getName() == null) {
            throw new IllegalStateException("Possible limits reached!! : " + json);
        }
        //Calculate actual enterprise value based on EVToEBITDA and EBITDA
        double enterpriseValue = avFinancialData.getEnterpriseValue() * avFinancialData.getEarnings();
        return avFinancialData.toBuilder()
                .setEnterpriseValue(enterpriseValue)
                .build();
    }

    @Override
    public Source getSource() {
        return Source.ALPHAVANTAGE;
    }
}