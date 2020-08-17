package com.dsi.adviser.integration.priceData;

import com.dsi.adviser.integration.client.PriceDataItem;
import com.dsi.adviser.integration.client.PriceHistorySource;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PriceDataProviderImpl implements PriceDataProvider {
    private static final int YEARS = 100;
    private final PriceDataRepository priceDataRepository;
    private final PriceHistorySource priceHistorySource;

    @Override
    public Flux<PriceData> getPriceData(String stockCodeFull, LocalDate fromDate) {
        return priceDataRepository.findFirstByStockCodeFullOrderByDateDesc(stockCodeFull)
                .map(PriceDataEntity::getDate)
                .switchIfEmpty(Mono.just(LocalDate.now().minusYears(YEARS)))
                .flatMapMany(lastDate -> selectFlow(stockCodeFull, lastDate, fromDate))
                .map(this::toPriceData);
    }

    private Flux<PriceDataEntity> selectFlow(String stockCodeFull, LocalDate lastDate, LocalDate fromDate) {
        //TODO: Move constant to application configurations
        if(ChronoUnit.DAYS.between(lastDate, LocalDate.now()) > 30) {
            return updateAndReturnData(stockCodeFull, lastDate)
                        .thenMany(loadDataFromDB(stockCodeFull, fromDate));
        }
        return loadDataFromDB(stockCodeFull, fromDate);
    }

    private Flux<PriceDataEntity> loadDataFromDB(String stockCodeFull, LocalDate fromDate) {
        if (null != fromDate) {
            return priceDataRepository.findAllByStockCodeFullAndDateGreaterThanEqual(stockCodeFull, fromDate);
        }
        return priceDataRepository.findAllByStockCodeFull(stockCodeFull);
    }

    private Mono<Void> updateAndReturnData(String stockCodeFull, LocalDate fromDate) {
        return priceHistorySource.getPriceHistory(stockCodeFull, fromDate)
                .filter(priceDataItem -> priceDataItem.getDate().isAfter(fromDate))
                .window(1000)
                .flatMap(priceDataFlux -> saveAll(priceDataFlux, stockCodeFull))
                .then();
    }

    private Flux<PriceDataItem> saveAll(Flux<PriceDataItem> priceDataFlux, String stockCodeFull) {
        Flux<PriceDataItem> dataFlux = priceDataFlux.cache();
        return dataFlux
                .map(priceData -> toTuples(priceData, stockCodeFull))
                .collectList()
                .flatMap(priceDataRepository::insertPriceDataEntities)
                .thenMany(dataFlux);
    }

    private Object[] toTuples(PriceDataItem priceData, String stockCodeFull) {
        return new Object[]{stockCodeFull, priceData.getDate(), priceData.getPriceOpen(),
                priceData.getPriceClose(), priceData.getPriceMin(), priceData.getPriceMax(), priceData.getVolume()};
    }

    private PriceData toPriceData(PriceDataEntity priceDataEntity) {
        return PriceModel.builder()
                .setStockCodeFull(priceDataEntity.getStockCodeFull())
                .setType(Period.DAY)
                .setDate(priceDataEntity.getDate())
                .setPrice(priceDataEntity.getPriceClose())
                .setPriceMax(priceDataEntity.getPriceMax())
                .setPriceMin(priceDataEntity.getPriceMin())
                .build();
    }
}