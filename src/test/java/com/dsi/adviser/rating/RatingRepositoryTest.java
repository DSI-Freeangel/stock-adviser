package com.dsi.adviser.rating;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

@DataR2dbcTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class RatingRepositoryTest {

    public static final RatingEntity RATING_ENTITY = RatingEntity.builder()
            .setDate(LocalDate.now())
            .setStockCodeFull("NYSE:IBM")
            .setApyGrown(0.1)
            .setDiscount(0.4)
            .setEarningValue(0.1)
            .setHyperbolic(0.2)
            .setBeauty(0.01)
            .build();
    @Autowired
    private RatingRepository ratingRepository;

    @After
    public void tearDown() {
        ratingRepository.deleteAll().block();
        System.out.println("RatingRepository is clear!!!");
    }

    @Test
    public void ratingSavedSuccessfully() {
        Mono<RatingEntity> ratingEntity = ratingRepository.save(RATING_ENTITY);

        StepVerifier.create(ratingEntity)
                .expectNextMatches(rating -> RATING_ENTITY.getDate().equals(rating.getDate())
                        && RATING_ENTITY.getStockCodeFull().equals(rating.getStockCodeFull())
                        && RATING_ENTITY.getApyGrown().equals(rating.getApyGrown())
                        && RATING_ENTITY.getDiscount().equals(rating.getDiscount())
                        && RATING_ENTITY.getEarningValue().equals(rating.getEarningValue())
                        && RATING_ENTITY.getHyperbolic().equals(rating.getHyperbolic())
                        && RATING_ENTITY.getBeauty().equals(rating.getBeauty()))
                .verifyComplete();
    }

}