package com.dsi.adviser.price;

import io.vavr.Predicates;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository repository;

    public Mono<PriceData> findLastAvailablePrice(String stockCode, Period period) {
        return repository.findFirstByStockCodeAndTypeOrderByDateDesc(stockCode, period)
                .map(this::toModel);
    }

    public Mono<Void> executeAggregationQueries(Mono<LocalDate> fromDate, String stockCode) {
        return fromDate.flatMap(date -> this.executeAggregationQueries(date, stockCode));
    }

    public Mono<Void> executeAggregationQueries(LocalDate fromDate, String stockCode) {
        return repository.executeMonthAggregationQuery(fromDate, fromDate, stockCode)
                .then(repository.executeQuarterAggregationQuery(fromDate, fromDate, stockCode))
                .then(repository.executeYearAggregationQuery(fromDate, stockCode));
    }

    public Flux<PriceData> saveAll(Flux<PriceData> priceDataFlux) {
        Flux<PriceData> dataFlux = priceDataFlux.cache();
        return dataFlux
                .map(this::toTuples)
                .collectList()
                .filter(Predicates.not(CollectionUtils::isEmpty))
                .flatMap(repository::insertPriceEntity)
                .thenMany(dataFlux);
    }

    private Object[] toTuples(PriceData priceData) {
        return new Object[]{priceData.getStockCode(), priceData.getType().name(), priceData.getDate(),
                priceData.getPrice(), priceData.getPriceMin(), priceData.getPriceMax()};
    }

    public Flux<PriceData> findPricesForInterval(String stockCode, Period period, LocalDate fromDate, LocalDate toDate) {
        return repository.findAllByStockCodeAndTypeAndDateGreaterThanEqualAndDateLessThanEqual(stockCode, period, fromDate, toDate)
                .map(this::toModel);
    }

    private PriceData toModel(PriceEntity priceEntity) {
        PriceModel.PriceModelBuilder priceModelBuilder = PriceModel.builder();
        BeanUtils.copyProperties(priceEntity, priceModelBuilder);
        return priceModelBuilder.build();
    }

}