package com.dsi.adviser.integration.financialData.parser;

import com.dsi.adviser.integration.financialData.Source;

public interface FinancialDataParser {
    RawFinancialData parse(String json);
    Source getSource();
}