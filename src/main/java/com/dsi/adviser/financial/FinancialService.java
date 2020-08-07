package com.dsi.adviser.financial;

import com.sun.istack.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FinancialService {
    private final FinancialRepository financialRepository;

    public Mono<Financial> findOneByStockCode(@NotNull String stockCode) {
        return Mono.just(stockCode)
                .map(financialRepository::findOneByStockCodeFullEquals)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::toModel);
    }

    public Mono<Financial> save(Financial financial) {
        return Mono.just(financial)
                .map(this::toEntity)
                .map(financialRepository::save)
                .map(this::toModel);
    }

    private Financial toModel(FinancialEntity financialEntity) {
        Financial.FinancialBuilder builder = Financial.builder();
        BeanUtils.copyProperties(financialEntity, builder);
        return builder.build();
    }

    private FinancialEntity toEntity(Financial financial) {
        FinancialEntity entity = financialRepository.findOneByStockCodeFullEquals(financial.getStockCodeFull())
                .orElseGet(FinancialEntity::new);
        BeanUtils.copyProperties(financial, entity);
        return entity;
    }
}
