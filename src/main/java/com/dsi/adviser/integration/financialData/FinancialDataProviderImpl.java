package com.dsi.adviser.integration.financialData;

import com.dsi.adviser.financial.FinancialData;
import com.dsi.adviser.financial.FinancialModel;
import com.dsi.adviser.integration.client.FinancialDataItem;
import com.dsi.adviser.integration.client.FinancialDataSource;
import com.dsi.adviser.integration.financialData.parser.FinancialDataParser;
import com.dsi.adviser.integration.financialData.parser.FinancialParserProvider;
import com.dsi.adviser.integration.financialData.parser.RawFinancialData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class FinancialDataProviderImpl implements FinancialDataProvider {
    private final FinancialDataSource dataSource;
    private final FinancialDataRepository financialDataRepository;
    private final FinancialParserProvider financialParserProvider;

    @Override
    public Mono<FinancialData> getFinancialData(String stockCodeFull) {
        Mono<FinancialDataEntity> existing = financialDataRepository.findOneByStockCodeFullEquals(stockCodeFull).cache();
        return existing.filter(financialDataEntity -> ChronoUnit.DAYS.between(financialDataEntity.getDate(), LocalDate.now()) < 30)
                .switchIfEmpty(this.loadActualData(stockCodeFull, existing))
                .map(this::toFinancialData);
    }

    private Mono<FinancialDataEntity> loadActualData(String stockCodeFull, Mono<FinancialDataEntity> existing) {
        return dataSource.getFinancialData(stockCodeFull)
                .flatMap(dataItem -> toEntity(existing, dataItem))
                .flatMap(financialDataRepository::save);
    }

    private Mono<FinancialDataEntity> toEntity(Mono<FinancialDataEntity> existing, FinancialDataItem dataItem) {
        return existing
                .map(FinancialDataEntity::toBuilder)
                .switchIfEmpty(Mono.just(FinancialDataEntity.builder()))
                .map(entityBuilder -> populateFields(entityBuilder, dataItem));
    }

    private FinancialDataEntity populateFields(FinancialDataEntity.FinancialDataEntityBuilder entityBuilder, FinancialDataItem dataItem) {
        BeanUtils.copyProperties(dataItem, entityBuilder);
        return entityBuilder.build();
    }

    private FinancialData toFinancialData(FinancialDataEntity financialDataEntity) {
        FinancialModel.FinancialModelBuilder builder = FinancialModel.builder()
                .setStockCodeFull(financialDataEntity.getStockCodeFull());
        Source source = financialDataEntity.getSource();
        FinancialDataParser parser = financialParserProvider.getParser(source);
        RawFinancialData financialData = parser.parse(financialDataEntity.getJsonResponse());
        BeanUtils.copyProperties(financialData, builder);
        return builder.build();
    }

}