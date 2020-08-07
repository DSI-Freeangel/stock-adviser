package com.dsi.adviser.financial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface FinancialRepository extends JpaRepository<FinancialEntity, Long> {
    Optional<FinancialEntity> findOneByStockCodeFullEquals(String stockCodeFull);
}
