package com.dsi.adviser.financial;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Repository
@Transactional
public interface FinancialRepository extends ReactiveCrudRepository<FinancialEntity, Long> {
    Mono<FinancialEntity> findOneByStockCodeFullEquals(String stockCodeFull);
}
