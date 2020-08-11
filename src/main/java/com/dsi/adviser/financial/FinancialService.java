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

    public Mono<FinancialModel> findOneByStockCode(@NotNull String stockCode) {
        return financialRepository.findOneByStockCodeFullEquals(stockCode)
                .map(this::toModel);
    }

    public Mono<FinancialModel> save(FinancialModel financial) {
        return toEntity(financial)
                .flatMap(financialRepository::save)
                .map(this::toModel);
    }

    private FinancialModel toModel(FinancialEntity financialEntity) {
        FinancialModel.FinancialModelBuilder builder = FinancialModel.builder();
        BeanUtils.copyProperties(financialEntity, builder);
        return builder.build();
    }

    private Mono<FinancialEntity> toEntity(FinancialModel financial) {
        return financialRepository.findOneByStockCodeFullEquals(financial.getStockCodeFull())
                .map(FinancialEntity::toBuilder)
                .switchIfEmpty(Mono.fromCallable(FinancialEntity::builder))
                .doOnNext( entity -> BeanUtils.copyProperties(financial, entity))
                .map(FinancialEntity.FinancialEntityBuilder::build);
    }
}
