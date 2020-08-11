package com.dsi.adviser.integration.priceData;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PriceDataRepository extends ReactiveCrudRepository<PriceDataEntity, Long> {
}
