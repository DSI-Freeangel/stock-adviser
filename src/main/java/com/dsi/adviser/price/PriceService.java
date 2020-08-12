package com.dsi.adviser.price;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository repository;
}