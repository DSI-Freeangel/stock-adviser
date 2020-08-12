package com.dsi.adviser.price;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PriceData {
    String getStockCodeFull();
    Period getType();
    LocalDate getDate();
    Double getPrice();
    Double getPriceMin();
    Double getPriceMax();
    LocalDateTime getCreatedDate();
    LocalDateTime getUpdatedDate();
}