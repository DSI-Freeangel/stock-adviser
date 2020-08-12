package com.dsi.adviser.integration.financialData.parser;

import com.dsi.adviser.integration.financialData.Source;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FinancialParserProvider {
    private final Map<Source, FinancialDataParser> parserMap;

    public FinancialParserProvider(List<FinancialDataParser> parsers) {
        this.parserMap = parsers.stream().collect(Collectors.toMap(FinancialDataParser::getSource, Function.identity()));
    }

    public FinancialDataParser getParser(Source source) {
        FinancialDataParser dataParser = parserMap.get(source);
        if(dataParser == null) {
            throw new IllegalStateException(String.format("Source '%s' is not configured or implemented yet!", source));
        }
        return dataParser;
    }
}
