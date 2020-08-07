package com.dsi.adviser.rating;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "PRICE")
public class RatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "STOCK_CODE_FULL")
    private String stockCodeFull;

    @Column(name = "BEAUTY")
    private Double beauty;

    @Column(name = "APY_GROWN")
    private Double apyGrown;

    @Column(name = "EARNING_VALUE")
    private Double earningValue;

    @Column(name = "HYPERBOLIC")
    private Double hyperbolic;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
