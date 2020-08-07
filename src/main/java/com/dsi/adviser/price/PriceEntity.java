package com.dsi.adviser.price;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "PRICE")
public class PriceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "STOCK_CODE_FULL")
    private String stockCodeFull;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private Period type;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "PRICE")
    private Double price;

    @Column(name = "PRICE_MIN")
    private Double priceMin;

    @Column(name = "PRICE_MAX")
    private Double priceMax;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
