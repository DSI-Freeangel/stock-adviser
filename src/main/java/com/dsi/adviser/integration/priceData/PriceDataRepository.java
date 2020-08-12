package com.dsi.adviser.integration.priceData;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
@Transactional
public interface PriceDataRepository extends ReactiveCrudRepository<PriceDataEntity, Long> {
    Flux<PriceDataEntity> findAllByStockCodeFullAndDateGreaterThanEqual(String stockCodeFull, LocalDate fromDate);
    Flux<PriceDataEntity> findAllByStockCodeFull(String stockCodeFull);
    Mono<PriceDataEntity> findFirstByStockCodeFullOrderByDateDesc(String stockCodeFull);
}
