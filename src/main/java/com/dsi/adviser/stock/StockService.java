package com.dsi.adviser.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService implements RemoveStockService{
    private final StockRepository stockRepository;

    public Mono<Stock> updateStock(Stock stock) {
        Mono<StockEntity> entityMono = stockRepository.findOneByStockCodeEquals(stock.getStockCode())
                .switchIfEmpty(Mono.fromCallable(StockEntity.builder()::build))
                .cache();
        return entityMono
                .map(this::toStockModel)
                .filter(fromDB -> !fromDB.equals(stock))
                .flatMap(existing -> entityMono)
                .map(StockEntity::toBuilder)
                .doOnNext(entityBuilder -> BeanUtils.copyProperties(stock, entityBuilder))
                .doOnNext(entityBuilder -> entityBuilder.setUpdatedDate(LocalDateTime.now()))
                .map(StockEntity.StockEntityBuilder::build)
                .flatMap(stockRepository::save)
                .map(this::toStockModel);
    }

    public Mono<Stock> updateStockIndustryData(IndustryData industryData) {
        return Mono.just(industryData)
                .map(this::toStockModel)
                .flatMap(this::updateStock)
                .doOnNext(v -> log.info("Stock '{}' industry data updated!", industryData.getStockCode()));
    }

    public Flux<Stock> findAll() {
        return stockRepository.findAllByActiveTrue()
                .map(this::toStockModel);
    }

    private Stock toStockModel(IndustryData industryData) {
        StockModel.StockModelBuilder modelBuilder = StockModel.builder();
        BeanUtils.copyProperties(industryData, modelBuilder);
        return modelBuilder
                .setStockCodeFull(industryData.getExchange() + ":" + industryData.getStockCode())
                .build();
    }

    private Stock toStockModel(StockEntity stockEntity) {
        StockModel.StockModelBuilder modelBuilder = StockModel.builder();
        BeanUtils.copyProperties(stockEntity, modelBuilder);
        return modelBuilder.build();
    }

    @Override
    public Mono<Void> removeByCode(String stockCode) {
        return stockRepository.findOneByStockCodeEquals(stockCode)
                .map(StockEntity::toBuilder)
                .map(builder -> builder.setActive(false))
                .map(StockEntity.StockEntityBuilder::build)
                .flatMap(stockRepository::save)
                .doOnNext(v -> log.info("Stock '{}' removed from rating scope!", stockCode))
                .then();
    }
}
