package com.dsi.adviser.stock;

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
@Table(name = "STOCK")
public class StockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CODE_FULL")
    private String codeFull;

    @Column(name = "CODE")
    private String code;

    @Column(name = "EXCHANGE")
    private String exchange;

    @Column(name = "SECTOR")
    private String sector;

    @Column(name = "INDUSTRY")
    private String industry;

    @Column(name = "DATA_UPDATED_DATE")
    private LocalDate dataUpdatedDate;

    @Column(name = "ACTIVE")
    private Boolean active = true;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
