package com.dsi.adviser.integration.client;

import java.time.LocalDate;

public interface PriceDataItem {
    LocalDate getDate();
    Double getPriceOpen();
    Double getPriceClose();
    Double getPriceMin();
    Double getPriceMax();
    Double getVolume();
}