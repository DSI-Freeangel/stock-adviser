package com.dsi.adviser.financial;

import com.sun.istack.internal.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FinancialService {
    private final FinancialRepository financialRepository;

    public Mono<FinancialData> findOneByStockCode(@NotNull String stockCode) {
        return financialRepository.findOneByStockCodeEquals(stockCode)
                .map(this::toModel);
    }

    public Mono<FinancialData> save(FinancialData financial) {
        return toEntity(financial)
                .flatMap(financialRepository::save)
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
                .doOnNext( entity -> BeanUtils.copyProperties(financial, entity))
                .map(FinancialEntity.FinancialEntityBuilder::build);
    }
}
