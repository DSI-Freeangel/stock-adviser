package com.dsi.adviser.integration.priceData;

import com.dsi.adviser.integration.client.PriceDataItem;
import com.dsi.adviser.integration.client.PriceHistorySource;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceDataProviderImpl implements PriceDataProvider {
    private static final int YEARS = 100;
    private final PriceDataRepository priceDataRepository;
    private final PriceHistorySource priceHistorySource;
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService loadExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService continueExecutor = Executors.newFixedThreadPool(2);

    @Override
    public Flux<PriceData> getPriceData(String stockCode, LocalDate fromDate) {
        return priceDataRepository.findFirstByStockCodeOrderByDateDesc(stockCode)
                .publishOn(Schedulers.fromExecutor(continueExecutor))
                .map(PriceDataEntity::getDate)
                .switchIfEmpty(Mono.just(LocalDate.now().minusYears(YEARS)))
                .filter(date -> ChronoUnit.DAYS.between(date, LocalDate.now()) > 30)
                .flatMap(lastDate -> updateAndReturnData(stockCode, lastDate))
                .publishOn(Schedulers.fromExecutor(loadExecutor))
                .thenMany(loadDataFromDB(stockCode, fromDate))
                .publishOn(Schedulers.fromExecutor(continueExecutor))
                .map(this::toPriceData);
    }

    private Flux<PriceDataEntity> loadDataFromDB(String stockCode, LocalDate fromDate) {
        if (null != fromDate) {
            return priceDataRepository.findAllByStockCodeAndDateGreaterThanEqual(stockCode, fromDate);
        }
        return priceDataRepository.findAllByStockCode(stockCode);
    }

    private Mono<Void> updateAndReturnData(String stockCode, LocalDate fromDate) {
        return priceHistorySource.getPriceHistory(stockCode, fromDate)
                .filter(priceDataItem -> priceDataItem.getDate().isAfter(fromDate))
                .transformDeferred(priceDataFlux -> saveAll(priceDataFlux, stockCode))
                .then();
    }

    private Mono<Void> saveAll(Flux<PriceDataItem> priceDataFlux, String stockCode) {
        return priceDataFlux
                .map(priceData -> toTuples(priceData, stockCode))
                .window(1000)
                .flatMap(Flux::collectList)
                .filter(list -> !CollectionUtils.isEmpty(list))
                .doOnNext((v) -> log.info("Going to save history for stock {}", stockCode))
                .publishOn(Schedulers.fromExecutor(saveExecutor))
                .flatMap(priceDataRepository::insertPriceDataEntities, 1, 1)
                .doOnNext((v) -> log.info("Price history saved for stock {}", stockCode))
                .then();
    }

    private Object[] toTuples(PriceDataItem priceData, String stockCode) {
        return new Object[]{stockCode, priceData.getDate(), priceData.getPriceOpen(),
                priceData.getPriceClose(), priceData.getPriceMin(), priceData.getPriceMax(), priceData.getVolume()};
    }

    private PriceData toPriceData(PriceDataEntity priceDataEntity) {
        return PriceModel.builder()
                .setStockCode(priceDataEntity.getStockCode())
                .setType(Period.DAY)
                .setDate(priceDataEntity.getDate())
                .setPrice(priceDataEntity.getPriceClose())
                .setPriceMax(priceDataEntity.getPriceMax())
                .setPriceMin(priceDataEntity.getPriceMin())
                .build();
    }
}