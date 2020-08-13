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
            Flux<PriceDataEntity> dataFromSource = updateAndReturnData(stockCodeFull, lastDate);
            if(null != fromDate) {
                dataFromSource = dataFromSource.filter(priceDataEntity -> priceDataEntity.getDate().isAfter(fromDate));
            }
            return loadDataFromDB(stockCodeFull, fromDate)
                    .concatWith(dataFromSource);
        }
        return loadDataFromDB(stockCodeFull, fromDate);
    }

    private Flux<PriceDataEntity> loadDataFromDB(String stockCodeFull, LocalDate fromDate) {
        if (null != fromDate) {
            return priceDataRepository.findAllByStockCodeFullAndDateGreaterThanEqual(stockCodeFull, fromDate);
        }
        return priceDataRepository.findAllByStockCodeFull(stockCodeFull);
    }

    private Flux<PriceDataEntity> updateAndReturnData(String stockCodeFull, LocalDate fromDate) {
        return priceHistorySource.getPriceHistory(stockCodeFull, fromDate)
                .filter(priceDataItem -> priceDataItem.getDate().isAfter(fromDate))
                .map(priceDataItem -> toEntity(priceDataItem, stockCodeFull))
                .window(1000)
                .flatMap(this::saveAll);
    }

    private Flux<PriceDataEntity> saveAll(Flux<PriceDataEntity> priceDataEntityFlux) {
        return priceDataRepository.saveAll(priceDataEntityFlux);
    }

    private PriceDataEntity toEntity(PriceDataItem priceDataItem, String stockCodeFull) {
        PriceDataEntity.PriceDataEntityBuilder builder = PriceDataEntity.builder();
        BeanUtils.copyProperties(priceDataItem, builder);
        return builder.setStockCodeFull(stockCodeFull).build();
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