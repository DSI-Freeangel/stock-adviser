package com.dsi.adviser.financial;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(setterPrefix = "set")
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Financial {
    private final String stockCodeFull;
    private final Integer year;
    private final Double enterpriseValue;
    private final Double earnings;
    private final Double dividendsApy;
    private final Double priceMinYtd;
    private final Double priceMaxYtd;
    private final LocalDateTime createdDate;
    private final LocalDateTime updatedDate;

}
