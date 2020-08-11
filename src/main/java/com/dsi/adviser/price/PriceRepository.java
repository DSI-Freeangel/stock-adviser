package com.dsi.adviser.price;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PriceRepository extends ReactiveCrudRepository<PriceEntity, Long> {
}
