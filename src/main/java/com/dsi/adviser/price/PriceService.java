package com.dsi.adviser.price;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository repository;

    public Mono<PriceData> findLastAvailablePrice(String stockCodeFull, Period day) {
        return null;
    }

    public Flux<PriceData> saveAll(Flux<PriceData> priceDataFlux) {
        return repository.saveAll(priceDataFlux.flatMap(this::toEntity))
                .map(this::toModel);
    }

    public Flux<PriceData> completeMonth(Mono<LocalDate> toDate) {
        //TODO: provide DAY records before lastDate in month
        return null;
    }

    public Flux<PriceData> completeQuarter(Mono<LocalDate> toDate) {
        //TODO: provide MONTH records before lastDate month in quarter
        return null;
    }

    public Flux<PriceData> completeYear(Mono<LocalDate> toDate) {
        //TODO: provide QUARTER records before lastDate quarter in year
        return null;
    }

    private PriceData toModel(PriceEntity priceEntity) {
        return null;
    }

    private Mono<PriceEntity> toEntity(PriceData priceData) {
        return null;
    }
}