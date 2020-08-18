package com.dsi.adviser.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    public Mono<Stock> updateStock(Stock stock) {
        Mono<StockEntity> entityMono = stockRepository.findOneByStockCodeFullEquals(stock.getStockCodeFull())
                .switchIfEmpty(Mono.fromCallable(StockEntity.builder()::build))
                .cache();
        return entityMono
                .map(this::toStockModel)
                .filter(fromDB -> !fromDB.equals(stock))
                .flatMap(existing -> entityMono)
                .map(StockEntity::toBuilder)
                .doOnNext(entityBuilder -> BeanUtils.copyProperties(stock, entityBuilder))
                .map(StockEntity.StockEntityBuilder::build)
                .flatMap(stockRepository::save)
                .map(this::toStockModel);
    }

    public Mono<Stock> updateStockIndustryData(IndustryData industryData) {
        return Mono.just(industryData)
                .map(this::toStockModel)
                .flatMap(this::updateStock);
    }

    private Stock toStockModel(IndustryData industryData) {
        StockModel.StockModelBuilder modelBuilder = StockModel.builder();
        BeanUtils.copyProperties(industryData, modelBuilder);
        String[] fullCodeSplit = industryData.getStockCodeFull().split(":");
        return modelBuilder.setCode(fullCodeSplit[1])
                .setExchange(fullCodeSplit[0])
                .build();
    }

    private Stock toStockModel(StockEntity stockEntity) {
        StockModel.StockModelBuilder modelBuilder = StockModel.builder();
        BeanUtils.copyProperties(stockEntity, modelBuilder);
        return modelBuilder.build();
    }

}
