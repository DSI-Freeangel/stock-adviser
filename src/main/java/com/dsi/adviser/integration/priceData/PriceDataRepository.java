package com.dsi.adviser.integration.priceData;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public interface PriceDataRepository extends ReactiveCrudRepository<PriceDataEntity, Long> {
    Flux<PriceDataEntity> findAllByStockCodeAndDateGreaterThanEqual(String stockCode, LocalDate fromDate);
    Flux<PriceDataEntity> findAllByStockCode(String stockCode);
    Mono<PriceDataEntity> findFirstByStockCodeOrderByDateDesc(String stockCode);

    @Query("INSERT INTO PRICE_DATA(STOCK_CODE, DATE, PRICE_OPEN, PRICE_CLOSE, PRICE_MIN, PRICE_MAX, VOLUME)\n" +
            "VALUES :tuples\n" +
            "ON DUPLICATE KEY UPDATE\n" +
            "  PRICE_OPEN = VALUES(PRICE_OPEN),\n" +
            "  PRICE_CLOSE = VALUES(PRICE_CLOSE),\n" +
            "  PRICE_MIN = VALUES(PRICE_MIN),\n" +
            "  PRICE_MAX = VALUES(PRICE_MAX),\n" +
            "  VOLUME = VALUES(VOLUME);")
    Mono<Void> insertPriceDataEntities(List<Object[]> tuples);
}
