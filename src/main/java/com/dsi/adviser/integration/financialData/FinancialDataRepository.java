package com.dsi.adviser.integration.financialData;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Repository
@Transactional
public interface FinancialDataRepository extends ReactiveCrudRepository<FinancialDataEntity, Long> {
    Mono<FinancialDataEntity> findOneByStockCodeFullEquals(String stockCodeFull);
}
