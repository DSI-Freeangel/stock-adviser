package com.dsi.adviser.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface StockRepository extends JpaRepository<StockEntity, Long> {
    Optional<StockEntity> findOneByCodeFullEquals(String stockCodeFull);
}
