package com.dsi.adviser.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;

    public Mono<Void> deleteAllByDate(LocalDate date) {
        return ratingRepository.deleteAllByDate(date);
    }

    public Flux<Rating> saveAll(Flux<Rating> ratings) {
        return ratingRepository
                .saveAll(ratings.map(this::toEntity))
                .map(this::toModel);
    }

    private Rating toModel(RatingEntity ratingEntity) {
        RatingModel.RatingModelBuilder builder = RatingModel.builder();
        BeanUtils.copyProperties(ratingEntity, builder);
        return builder.build();
    }

    private RatingEntity toEntity(Rating rating) {
        RatingEntity.RatingEntityBuilder builder = RatingEntity.builder();
        BeanUtils.copyProperties(rating, builder);
        return builder.build();
    }
}
