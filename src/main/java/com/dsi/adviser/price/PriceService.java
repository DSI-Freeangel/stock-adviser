package com.dsi.adviser.price;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository repository;

    public Mono<PriceData> findLastAvailablePrice(String stockCodeFull, Period period) {
        return repository.findFirstByStockCodeFullAndTypeOrderByDateDesc(stockCodeFull, period)
                .map(this::toModel);
    }

    public Mono<Void> executeAggregationQueries(Mono<LocalDate> fromDate, String stockCodeFull) {
        return fromDate.flatMap(date -> this.executeAggregationQueries(date, stockCodeFull));
    }

    public Mono<Void> executeAggregationQueries(LocalDate fromDate, String stockCodeFull) {
        return repository.executeMonthAggregationQuery(fromDate, fromDate, stockCodeFull)
                .then(repository.executeQuarterAggregationQuery(fromDate, fromDate, stockCodeFull))
                .then(repository.executeYearAggregationQuery(fromDate, stockCodeFull));
    }

    public Flux<PriceData> saveAll(Flux<PriceData> priceDataFlux) {
        Flux<PriceData> dataFlux = priceDataFlux.cache();
        return dataFlux
                .map(this::toTuples)
                .collectList()
                .flatMap(repository::insertPriceEntity)
                .thenMany(dataFlux);
    }

    private Object[] toTuples(PriceData priceData) {
        return new Object[]{priceData.getStockCodeFull(), priceData.getType(), priceData.getDate(),
                priceData.getPrice(), priceData.getPriceMin(), priceData.getPriceMax()};
    }

    public Flux<PriceData> findPricesForInterval(String stockCodeFull, Period period, LocalDate fromDate, LocalDate toDate) {
        return repository.findAllByStockCodeFullAndTypeAndDateGreaterThanEqualAndDateLessThanEqual(stockCodeFull, period, fromDate, toDate)
                .map(this::toModel);
    }

    private PriceData toModel(PriceEntity priceEntity) {
        PriceModel.PriceModelBuilder priceModelBuilder = PriceModel.builder();
        BeanUtils.copyProperties(priceEntity, priceModelBuilder);
        return priceModelBuilder.build();
    }

    private Mono<PriceEntity> toEntity(PriceData priceData) {
        return repository.findFirstByStockCodeFullAndTypeAndDate(priceData.getStockCodeFull(), priceData.getType(), priceData.getDate())
                .map(PriceEntity::toBuilder)
                .switchIfEmpty(Mono.just(PriceEntity.builder()
                        .setStockCodeFull(priceData.getStockCodeFull())
                        .setDate(priceData.getDate())
                        .setType(priceData.getType())))
                .map(priceEntityBuilder -> priceEntityBuilder
                        .setPrice(priceData.getPrice())
                        .setPriceMin(priceData.getPriceMin())
                        .setPriceMax(priceData.getPriceMax()))
                .map(PriceEntity.PriceEntityBuilder::build);
    }
}