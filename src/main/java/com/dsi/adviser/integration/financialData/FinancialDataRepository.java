package com.dsi.adviser.integration.financialData;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface FinancialDataRepository extends ReactiveCrudRepository<FinancialDataEntity, Long> {
}
