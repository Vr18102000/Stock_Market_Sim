package com.sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sim.model.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

    // Add methods to find stocks by symbol
    Stock findBySymbol(String symbol);
}
