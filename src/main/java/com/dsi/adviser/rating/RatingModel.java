package com.dsi.adviser.rating;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RatingModel implements Rating {
    String stockCodeFull;
    LocalDate date;
    Double beauty;
    Double apyGrown;
    Double discount;
    Double earningValue;
    Double hyperbolic;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
}
