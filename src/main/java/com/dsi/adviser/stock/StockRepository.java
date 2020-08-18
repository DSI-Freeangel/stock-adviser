package com.dsi.adviser.stock;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Repository
@Transactional
public interface StockRepository extends ReactiveCrudRepository<StockEntity, Long> {
    Mono<StockEntity> findOneByStockCodeFullEquals(String stockCodeFull);
}
