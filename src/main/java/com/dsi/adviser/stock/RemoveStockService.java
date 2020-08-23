package com.dsi.adviser.stock;

import reactor.core.publisher.Mono;

public interface RemoveStockService {
    Mono<Void> removeByCode(String stockCode);
}
