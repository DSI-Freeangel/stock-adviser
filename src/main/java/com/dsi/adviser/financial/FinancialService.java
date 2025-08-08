package com.dsi.adviser.financial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialService {
    private final FinancialRepository financialRepository;

    public Mono<FinancialData> findOneByStockCode(String stockCode) {
        return financialRepository.findOneByStockCodeEquals(stockCode)
                .map(this::toModel);
    }

    public Mono<FinancialData> save(FinancialData financial) {
        return toEntity(financial)
                .flatMap(financialRepository::save)
                .doOnNext(v -> log.info("Stock '{}' financial data updated!", financial.getStockCode()))
                .map(this::toModel);
    }

    private FinancialData toModel(FinancialEntity financialEntity) {
        FinancialModel.FinancialModelBuilder builder = FinancialModel.builder();
        BeanUtils.copyProperties(financialEntity, builder);
        return builder.build();
    }

    private Mono<FinancialEntity> toEntity(FinancialData financial) {
        return financialRepository.findOneByStockCodeEquals(financial.getStockCode())
                .map(FinancialEntity::toBuilder)
                .switchIfEmpty(Mono.fromCallable(FinancialEntity::builder))
                .doOnNext( entity -> BeanUtils.copyProperties(financial, entity, "createdDate", "updatedDate"))
                .doOnNext(entity -> entity.setUpdatedDate(LocalDateTime.now()))
                .map(FinancialEntity.FinancialEntityBuilder::build);
    }
}
