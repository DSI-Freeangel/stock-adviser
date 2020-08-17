package com.dsi.adviser.price;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public interface PriceRepository extends ReactiveCrudRepository<PriceEntity, Long> {

    @Query("INSERT INTO PRICE(STOCK_CODE_FULL, TYPE, DATE, PRICE, PRICE_MIN, PRICE_MAX)\n" +
            "SELECT\n" +
            "        STOCK_CODE_FULL, 'MONTH',\n" +
            "        CONCAT_WS('-', YEAR(DATE), MONTH(DATE), 1) AS FIRST_DAY,\n" +
            "       AVG(PRICE), MIN(PRICE_MIN), MAX(PRICE_MAX)\n" +
            "FROM PRICE\n" +
            "WHERE\n" +
            "  TYPE = 'DAY'\n" +
            "  AND DATE >= CONCAT_WS('-', YEAR(:1), MONTH(:2), 1)\n" +
            "  AND STOCK_CODE_FULL = :3\n" +
            "GROUP BY FIRST_DAY\n" +
            "ON DUPLICATE KEY UPDATE\n" +
            "  PRICE = VALUES(PRICE),\n" +
            "  PRICE_MIN = VALUES(PRICE_MIN),\n" +
            "  PRICE_MAX = VALUES(PRICE_MAX);")
    Mono<Void> executeMonthAggregationQuery(LocalDate fromDate, LocalDate fromDate1, String stockCodeFull);

    @Query("INSERT INTO PRICE(STOCK_CODE_FULL, TYPE, DATE, PRICE, PRICE_MIN, PRICE_MAX)\n" +
            "SELECT\n" +
            "         STOCK_CODE_FULL, 'QUARTER',\n" +
            "         CONCAT_WS('-', YEAR(DATE), (FLOOR((MONTH(DATE) - 1) / 3) + 1) * 3 - 2, 1) AS FIRST_DAY,\n" +
            "       AVG(PRICE), MIN(PRICE_MIN), MAX(PRICE_MAX)\n" +
            "FROM PRICE\n" +
            "WHERE\n" +
            "  TYPE = 'MONTH'\n" +
            "  AND DATE >= CONCAT_WS('-', YEAR(:1), (FLOOR((MONTH(:2) - 1) / 3) + 1) * 3 - 2, 1)\n" +
            "  AND STOCK_CODE_FULL = :3\n" +
            "GROUP BY FIRST_DAY\n" +
            "ON DUPLICATE KEY UPDATE\n" +
            "  PRICE = VALUES(PRICE),\n" +
            "  PRICE_MIN = VALUES(PRICE_MIN),\n" +
            "  PRICE_MAX = VALUES(PRICE_MAX);")
    Mono<Void> executeQuarterAggregationQuery(LocalDate fromDate, LocalDate fromDate1, String stockCodeFull);

    @Query("INSERT INTO PRICE(STOCK_CODE_FULL, TYPE, DATE, PRICE, PRICE_MIN, PRICE_MAX)\n" +
            "SELECT\n" +
            "        STOCK_CODE_FULL, 'YEAR',\n" +
            "        CONCAT_WS('-', YEAR(DATE), 1, 1) AS FIRST_DAY,\n" +
            "       AVG(PRICE), MIN(PRICE_MIN), MAX(PRICE_MAX)\n" +
            "FROM PRICE\n" +
            "WHERE\n" +
            "  TYPE = 'QUARTER'\n" +
            "  AND DATE >= CONCAT_WS('-', YEAR(:1), 1, 1)\n" +
            "  AND STOCK_CODE_FULL = :2\n" +
            "GROUP BY FIRST_DAY\n" +
            "ON DUPLICATE KEY UPDATE\n" +
            "  PRICE = VALUES(PRICE),\n" +
            "  PRICE_MIN = VALUES(PRICE_MIN),\n" +
            "  PRICE_MAX = VALUES(PRICE_MAX);")
    Mono<Void> executeYearAggregationQuery(LocalDate fromDate, String stockCodeFull);

    Flux<PriceEntity> findAllByStockCodeFullAndTypeAndDateGreaterThanEqual(String stockCodeFull, Period period, LocalDate fromDate);

    Flux<PriceEntity> findAllByStockCodeFullAndTypeAndDateGreaterThanEqualAndDateLessThanEqual(String stockCodeFull, Period period, LocalDate fromDate, LocalDate toDate);

    Mono<PriceEntity> findFirstByStockCodeFullAndTypeOrderByDateDesc(String stockCodeFull, Period period);

    Mono<PriceEntity> findFirstByStockCodeFullAndTypeAndDate(String stockCodeFull, Period period, LocalDate exactDate);

    @Query("INSERT INTO PRICE(STOCK_CODE_FULL, TYPE, DATE, PRICE, PRICE_MIN, PRICE_MAX)\n" +
            "VALUES :tuples\n" +
            "ON DUPLICATE KEY UPDATE\n" +
            "  PRICE = VALUES(PRICE),\n" +
            "  PRICE_MIN = VALUES(PRICE_MIN),\n" +
            "  PRICE_MAX = VALUES(PRICE_MAX);")
    Mono<Void> insertPriceEntity(List<Object[]> tuples);

}
