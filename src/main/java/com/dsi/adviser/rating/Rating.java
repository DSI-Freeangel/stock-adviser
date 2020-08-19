package com.dsi.adviser.rating;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface Rating {
    String getStockCodeFull();
    LocalDate getDate();
    Double getBeauty();
    Double getApyGrown();
    Double getDiscount();
    Double getEarningValue();
    Double getHyperbolic();
    LocalDateTime getCreatedDate();
    LocalDateTime getUpdatedDate();
}
