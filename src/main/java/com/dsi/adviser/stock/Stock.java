package com.dsi.adviser.stock;

public interface Stock extends IndustryData {
    String getStockCodeFull();
    String getCode();
    String getExchange();
}
