package com.dsi.adviser.rating;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
@Transactional
public interface RatingRepository extends ReactiveCrudRepository<RatingEntity, Long> {
    Mono<Void> deleteAllByDate(LocalDate date);
}
