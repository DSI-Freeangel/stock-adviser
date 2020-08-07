package com.dsi.adviser.price;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface PriceRepository extends JpaRepository<PriceEntity, Long> {
}
